package com.rwgs.scalapostgres.tutorial.persistence

import cats.effect.{Effect, IO, Sync}
import com.rwgs.scalapostgres.tutorial.persistence.models.{AccountStatus, AggregationType, FetchRequestEnding, LoginDetails}
import doobie.implicits._
import doobie.util.transactor.Transactor
import org.joda.time.format.ISODateTimeFormat
import java.sql.Timestamp
import java.util.UUID
import org.joda.time.{DateTime, DateTimeZone}
import org.joda.time.format.DateTimeFormatter

trait LoginAccountsRepository[F[_]] {
  def getPrimaryLoginDetailsByUserId(userId: String): Option[LoginDetails]

  def getCountOfActiveLoginAccountsByAccountStatusLastUpdated(
     status: AccountStatus.Value,
     date: DateTime,
     aggregationTypes: List[AggregationType.Value],
     ignorableInstitutions: Seq[String] = Seq.empty,
     fixed: Option[Boolean] = None): F[Int]

  def checkInActiveLoginAccountsByAccountStatusLastUpdated(
      status: AccountStatus.Value,
      changedEarlierThan: DateTime,
      aggregationTypes: List[AggregationType.Value]): F[Int]

}

object LoginAccountsRepository {
  import LoginAccountsQueries._

  def build[F[_]: Sync ](transactor: Transactor[F])(implicit F: Effect[F]): LoginAccountsRepository[F] =
    new LoginAccountsRepository[F] {

      def toIO[A](f: F[A]): IO[A] =
        IO.async { cb =>
          F.runAsync(f)(r => IO(cb(r))).unsafeRunSync()
        }

      override def getPrimaryLoginDetailsByUserId(userId: String): Option[LoginDetails] = {
        val loginDetailsF =
          LoginAccountsQueries.getPrimaryLoginDetailsByUserIdQuery(userId).option.transact(transactor)

        toIO(loginDetailsF).unsafeRunSync()
      }

      override def getCountOfActiveLoginAccountsByAccountStatusLastUpdated(
            status: AccountStatus.Value,
            date: DateTime,
            aggregationTypes: List[AggregationType.Value],
            ignorableInstitutions: Seq[String] = Seq.empty,
            fixed: Option[Boolean] = None): F[Int] =
        getCountOfActiveLoginAccountsByAccountStatusLastUpdatedQuery(
          status,
          date,
          aggregationTypes,
          ignorableInstitutions,
          fixed)
          .unique
          .transact(transactor)

      // idea from com.banno.fetch.reporting.PostgresLoginCheckinRepository's checkInAllLoginsForStatus
      override def checkInActiveLoginAccountsByAccountStatusLastUpdated(
           status: AccountStatus.Value,
           changedEarlierThan: DateTime,
           aggregationTypes: List[AggregationType.Value]): F[Int] =
        if (status == AccountStatus.NEEDS_USER_INTERVENTION || status == AccountStatus.NEEDS_OUTSIDE_INTERVENTION) {
          F.pure(0)
        } else {
          LoginAccountsQueries
            .checkInActiveLoginAccountsByAccountStatusLastUpdatedQuery(status, changedEarlierThan, aggregationTypes)
            .run
            .transact(transactor)
        }
    }

}

object LoginAccountsQueries {

  import doobie._
  import doobie.implicits.javasql._                    // need this for "could not find implicit value for parameter ev: doobie.util.meta.Meta[java.sql.Timestamp]"
  import io.chrisdavenport.fuuid.doobie.implicits._    // need this for "Cannot find or construct a Read instance for type (com.rwgs.scalapostgres.tutorial.persistence.models.LoginDetails)"

  implicit val uuidMeta: Meta[UUID] = Meta[String].imap(UUID.fromString)(_.toString)
  implicit val fetchRequestEndingMeta: Meta[FetchRequestEnding.Value] = Meta[Int].imap(FetchRequestEnding(_))(_.id)
  implicit val aggregationTypeMeta: Meta[AggregationType.Value] = Meta[Int].imap(AggregationType(_))(_.id)
  implicit val accountStatusMeta: Meta[AccountStatus.Value] = Meta[Int].imap(AccountStatus(_))(_.id)
  implicit val dateTimeComposite: Meta[DateTime] =
    Meta[Timestamp].imap(new DateTime(_).withZone(DateTimeZone.UTC))(dt => new Timestamp(dt.withZone(DateTimeZone.UTC).getMillis))

  // 2021-03-21 13:42:09.707
//  val dbDateFormatter: DateTimeFormatter = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss")
  val format: DateTimeFormatter = ISODateTimeFormat.dateTime()
  val utcTimeZone: DateTimeZone = DateTimeZone.UTC

  private[this] val loginAccountsFieldsFragment: Fragment =
    fr"""
        login_id,
        institution_id,
        status,
        last_status_change,
        is_out_of_band,
        last_fetch_ending,
        fixed,
        created_at,
        updated_at,
        user_id,
        active,
        primary_login,
        is_billpay,
        aggregation_type,
        last_successful,
        customer_id,
        netteller_id,
        member_number,
        failed_retrieval_attempts
      """

  private[this] val loginAccountsQueryFragment: Fragment =
    fr"SELECT" ++ loginAccountsFieldsFragment ++ fr"FROM login_accounts"

  def getPrimaryLoginDetailsByUserIdQuery(userId: String): Query0[LoginDetails] =
    (loginAccountsQueryFragment ++
      fr"""WHERE user_id = $userId
           AND active
           AND primary_login
           ORDER BY created_at DESC
         """
      ).query[LoginDetails]

  def getCountOfActiveLoginAccountsByAccountStatusLastUpdatedQuery(
        status: AccountStatus.Value,
        date: DateTime,
        aggregationTypes: List[AggregationType.Value],
        ignorableInstitutions: Seq[String] = Seq.empty,
        fixed: Option[Boolean] = None): Query0[Int] = {

    val iAggregationTypes = aggregationTypes
      .map(_.id.toString)
      .mkString(",")

    val strIgnorableInstitutions = ignorableInstitutions
      .map(v => s"'$v'")
      .mkString(",")

    //format.print(dateTime.toDateTime(utcTimeZone))
    val changedFormatted: String = format.print(date.toDateTime(utcTimeZone))

    sql"""
      SELECT count(*)
      FROM login_accounts
      WHERE status = ${status.id}
      AND (primary_login OR NOT is_billpay)
      AND date(last_status_change) < $changedFormatted::date
      AND (aggregation_type::text) IN ($iAggregationTypes)
      AND active
      AND NOT is_out_of_band
      AND fixed = ${fixed.getOrElse(false)}
      AND NOT (institution_id IN ($strIgnorableInstitutions))
    """.query[Int]
  }

  def checkInActiveLoginAccountsByAccountStatusLastUpdatedQuery(
    status: AccountStatus.Value,
    changedEarlierThan: DateTime,
    aggregationTypes: List[AggregationType.Value]): Update0 = {

    val iAggregationTypes: List[Int] = aggregationTypes.map(_.id)
    val changedFormatted: String = format.print(changedEarlierThan.toDateTime(utcTimeZone))

    val query = sql"""
        UPDATE login_accounts SET fixed = true
        WHERE status = ${status.id}
        AND date(last_status_change) < $changedFormatted::date
        AND (login_accounts.primary_login OR NOT login_accounts.is_billpay)
        AND (login_accounts.aggregation_type::text) IN (${iAggregationTypes.mkString(",")})
        AND login_accounts.active
        AND NOT login_accounts.is_out_of_band
        AND NOT login_accounts.fixed
      """

    query.update
  }

}
