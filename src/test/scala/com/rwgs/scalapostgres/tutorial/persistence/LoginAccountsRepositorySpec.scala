package com.rwgs.scalapostgres.tutorial.persistence

import cats.effect.{ContextShift, IO}
import com.rwgs.scalapostgres.tutorial.persistence.models.{AccountStatus, AggregationType, LoginDetails}
import org.joda.time.DateTime
import org.specs2.mutable.Specification

class LoginAccountsRepositorySpec extends Specification {

  "retrieve a count of Active loginAccounts that haven't ran since a certain date" in new context {
    import com.rwgs.scalapostgres.tutorial.persistence.models.AggregationType.ONLINE_BANKING

    insertLoginDetailsTestQuery(testLoginDetails01).unsafeRunSync()
    insertLoginDetailsTestQuery(testLoginDetails04).unsafeRunSync()
    insertLoginDetailsTestQuery(testLoginDetails05).unsafeRunSync()

    val count: Int =
      loginAccountsRepo.
        getCountOfActiveLoginAccountsByAccountStatusLastUpdated(
          AccountStatus.NORMAL,
          DateTime.now.minusDays(1),
          List(ONLINE_BANKING)
        ).unsafeRunSync()

    count must be_==(1)
  }

  import org.specs2.matcher.Scope
  trait context extends Scope {
    import cats.syntax.all._                  //needed for ".show" call
    import LoginAccountsQueries._
    import doobie._
    import doobie.implicits._

    import scala.concurrent.ExecutionContext  //needed for "Cannot find an implicit value for ContextShift[cats.effect.IO]"
    implicit val cs: ContextShift[IO] = IO.contextShift(ExecutionContext.global)

    import doobie.Transactor
    //TODO - for Transactor, ask:
    // 1) Transactor.fromDriverManager() ???
    // 2) or use "Transactor.fromDataSource"???
    lazy val transactor: Transactor[IO] = Transactor.fromDriverManager[IO](
      "org.postgresql.Driver",
      "jdbc:postgresql://localhost:5432/postgres",
      "postgres",
      "password"
    )

    def clearLoginAccountsDbTable(): IO[Unit] = {
      {sql"""
             DELETE FROM login_accounts""".update
      }.run.transact(transactor).void
    }

    import doobie.implicits._  // for the Doobie "sql" call

    val loginAccountsRepo: LoginAccountsRepository[IO] = LoginAccountsRepository.build[IO](transactor)
    def uuidToString(): String = java.util.UUID.randomUUID.toString

    def insertLoginDetailsTestQuery(details: LoginDetails): IO[Unit] = {
      {
        sql"""
            INSERT INTO login_accounts (
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
              username,
              last_successful,
              customer_id,
              netteller_id,
              member_number,
              failed_retrieval_attempts,
              first_logged_in_dt)
          VALUES
          (${details.loginId},
           ${details.institutionId},
           ${details.status},
           ${details.lastStatusChangeDate},
           ${details.isOutOfBand},
           ${details.lastFetchEnding},
           ${details.fixed},
           ${details.createdAt},
           ${details.updatedAt},
           ${details.userId},
           ${details.active},
           ${details.primaryLogin},
           ${details.isBillPay},
           ${details.aggregationType},
           ${details.username},
           ${details.lastSuccessfulEnding},
           ${details.customerId},
           ${details.nettellerId},
           ${details.memberNumber},
           ${details.failedRetrievalAttempts},
           ${details.firstTimeLoggedIn}
           )""".update
      }.run.transact(transactor).void
    }

    val testLoginDetails01: LoginDetails =
      LoginDetails(
        loginId = "testLoginId1",
        institutionId ="inst1",
        status = AccountStatus.NORMAL,
        lastStatusChangeDate = DateTime.now,
        isOutOfBand = false,
        lastFetchEnding = None,
        fixed = false,
        createdAt = DateTime.now,
        updatedAt = DateTime.now,
        userId = "user1",
        active = true,
        primaryLogin = true,
        isBillPay = false,
        aggregationType = AggregationType.ONLINE_BANKING,
        username = "username01",
        lastSuccessfulEnding = None,
        customerId = None,
        nettellerId = None,
        memberNumber = None,
        failedRetrievalAttempts = 0,
        firstTimeLoggedIn = None)

    val testLoginDetails04: LoginDetails =
      LoginDetails(
        loginId = "testLoginId4",
        institutionId ="inst1",
        status = AccountStatus.NORMAL,
        lastStatusChangeDate = DateTime.now.minusDays(2),
        isOutOfBand = false,
        lastFetchEnding = None,
        fixed = false,
        createdAt = DateTime.now,
        updatedAt = DateTime.now,
        userId = uuidToString(),
        active = true,
        primaryLogin = true,
        isBillPay = false,
        aggregationType = AggregationType.ONLINE_BANKING,
        username = "username04",
        lastSuccessfulEnding = None,
        customerId = None,
        nettellerId = None,
        memberNumber = None,
        failedRetrievalAttempts = 0,
        firstTimeLoggedIn = None)

    val testLoginDetails05: LoginDetails =
      LoginDetails(
        loginId = "testLoginId5",
        institutionId ="inst1",
        status = AccountStatus.NORMAL,
        lastStatusChangeDate = DateTime.now.minusDays(2),
        isOutOfBand = false,
        lastFetchEnding = None,
        fixed = false,
        createdAt = DateTime.now,
        updatedAt = DateTime.now,
        userId = uuidToString(),
        active = true,
        primaryLogin = false,
        isBillPay = true,
        aggregationType = AggregationType.ONLINE_BANKING,
        username = "username05",
        lastSuccessfulEnding = None,
        customerId = None,
        nettellerId = None,
        memberNumber = None,
        failedRetrievalAttempts = 0,
        firstTimeLoggedIn = None)

    val testLoginDetails08: LoginDetails =
      LoginDetails(
        loginId = "testLoginId8",
        institutionId ="inst5",
        status = AccountStatus.NORMAL,
        lastStatusChangeDate = DateTime.now.minusDays(2),
        isOutOfBand = false,
        lastFetchEnding = None,
        fixed = false,
        createdAt = DateTime.now,
        updatedAt = DateTime.now,
        userId = "user5",
        active = true,
        primaryLogin = true,
        isBillPay = false,
        aggregationType = AggregationType.NETTELLER,
        username = "username07",
        lastSuccessfulEnding = None,
        customerId = None,
        nettellerId = Some("fakeNetTellerId"),
        memberNumber = None,
        failedRetrievalAttempts = 0,
        firstTimeLoggedIn = None)

    val testLoginDetails09: LoginDetails =
      LoginDetails(
        loginId = "testLoginId9",
        institutionId ="inst5",
        status = AccountStatus.NORMAL,
        lastStatusChangeDate = DateTime.now.minusDays(2),
        isOutOfBand = false,
        lastFetchEnding = None,
        fixed = false,
        createdAt = DateTime.now,
        updatedAt = DateTime.now,
        userId = uuidToString(),
        active = true,
        primaryLogin = true,
        isBillPay = false,
        aggregationType = AggregationType.NETTELLER,
        username = "username08",
        lastSuccessfulEnding = None,
        customerId = None,
        nettellerId = None,
        memberNumber = Some("fakeMemberNumber"),
        failedRetrievalAttempts = 1,
        firstTimeLoggedIn = None)

    // below are test data for LoginDetails Batch Failures:

    val testLoginDetails10: LoginDetails =
      LoginDetails(
        loginId = "testLoginId10",
        institutionId ="inst4",
        status = AccountStatus.BATCH_FAILURE,
        lastStatusChangeDate = DateTime.now.minusDays(2),
        isOutOfBand = false,
        lastFetchEnding = None,
        fixed = false,
        createdAt = DateTime.now,
        updatedAt = DateTime.now,
        userId = uuidToString(),
        active = true,
        primaryLogin = true,
        isBillPay = false,
        aggregationType = AggregationType.CORE,
        username = "username07",
        lastSuccessfulEnding = None,
        customerId = None,
        nettellerId = None,
        memberNumber = None,
        failedRetrievalAttempts = 2,
        firstTimeLoggedIn = None)

    val testLoginDetails11: LoginDetails =
      LoginDetails(
        loginId = "testLoginId11",
        institutionId ="inst1",
        status = AccountStatus.BATCH_FAILURE,
        lastStatusChangeDate = DateTime.now.minusDays(2),
        isOutOfBand = false,
        lastFetchEnding = None,
        fixed = false,
        createdAt = DateTime.now,
        updatedAt = DateTime.now,
        userId = uuidToString(),
        active = true,
        primaryLogin = true,
        isBillPay = false,
        aggregationType = AggregationType.CORE,
        username = "username01",
        lastSuccessfulEnding = None,
        customerId = None,
        nettellerId = None,
        memberNumber = None,
        failedRetrievalAttempts = 1,
        firstTimeLoggedIn = None)

    val testLoginDetails12: LoginDetails =
      LoginDetails(
        loginId = "testLoginId12",
        institutionId ="inst1",
        status = AccountStatus.BATCH_FAILURE,
        lastStatusChangeDate = DateTime.now.minusDays(2),
        isOutOfBand = false,
        lastFetchEnding = None,
        fixed = true,
        createdAt = DateTime.now,
        updatedAt = DateTime.now,
        userId = uuidToString(),
        active = true,
        primaryLogin = true,
        isBillPay = false,
        aggregationType = AggregationType.CORE,
        username = "username01",
        lastSuccessfulEnding = None,
        customerId = None,
        nettellerId = None,
        memberNumber = None,
        failedRetrievalAttempts = 1,
        firstTimeLoggedIn = None)

    val firstPrimaryLogin: LoginDetails =
      LoginDetails(
        loginId = "firstPrimaryLogin",
        institutionId ="inst4",
        status = AccountStatus.NORMAL,
        lastStatusChangeDate = DateTime.now.minusMinutes(2),
        isOutOfBand = false,
        lastFetchEnding = None,
        fixed = false,
        createdAt = DateTime.now.minusDays(1),
        updatedAt = DateTime.now,
        userId = uuidToString(),
        active = true,
        primaryLogin = true,
        isBillPay = false,
        aggregationType = AggregationType.CORE,
        username = "username07",
        lastSuccessfulEnding = None,
        customerId = None,
        nettellerId = None,
        memberNumber = None,
        failedRetrievalAttempts = 0,
        firstTimeLoggedIn = None)

    val latestPrimaryLogin: LoginDetails =
      LoginDetails(
        loginId = "latestPrimaryLogin",
        institutionId ="inst4",
        status = AccountStatus.NORMAL,
        lastStatusChangeDate = DateTime.now,
        isOutOfBand = false,
        lastFetchEnding = None,
        fixed = false,
        createdAt = DateTime.now,
        updatedAt = DateTime.now,
        userId = uuidToString(),
        active = true,
        primaryLogin = true,
        isBillPay = false,
        aggregationType = AggregationType.CORE,
        username = "username07",
        lastSuccessfulEnding = None,
        customerId = None,
        nettellerId = None,
        memberNumber = None,
        failedRetrievalAttempts = 0,
        firstTimeLoggedIn = None)

    // below are test data for LoginDetails DSO:
    def createLoginsWithCustomFields(
                                   status: AccountStatus.Value,
                                   lastStatusChange: DateTime,
                                   active: Boolean,
                                   aggregationType: AggregationType.Value,
                                   dso: DSO.Value): LoginDetails = {
      LoginDetails(
        loginId = uuidToString(),
        institutionId = uuidToString(),
        status = status,
        lastStatusChangeDate = lastStatusChange,
        isOutOfBand = false,
        lastFetchEnding = None,
        fixed = false,
        createdAt = lastStatusChange.minusDays(3),
        updatedAt = lastStatusChange.minusDays(3),
        userId = uuidToString(),
        active = active,
        primaryLogin = dso == DSO.IsNotDSOBillPayLogin,
        isBillPay = dso == DSO.IsDSOBillPayLogin,
        aggregationType = aggregationType,
        username = "usernameTest01",
        lastSuccessfulEnding = None,
        customerId = None,
        nettellerId = None,
        memberNumber = None,
        failedRetrievalAttempts = 0,
        firstTimeLoggedIn = None
      )
    }

  }

}

object DSO extends Enumeration {
  val IsDSOBillPayLogin: DSO.Value = Value
  val IsNotDSOBillPayLogin: DSO.Value = Value
}
