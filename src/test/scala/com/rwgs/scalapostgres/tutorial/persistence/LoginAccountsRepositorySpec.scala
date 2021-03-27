package com.rwgs.scalapostgres.tutorial.persistence

import cats.effect.{ContextShift, IO}
import cats.syntax.all._
import com.rwgs.scalapostgres.tutorial.persistence.models.{AccountStatus, AggregationType, LoginDetails}
import com.rwgs.scalapostgres.tutorial.persistence.models.AggregationType._
//import io.chrisdavenport.fuuid.FUUID
import org.joda.time.DateTime
import org.specs2.mutable.Specification

class LoginAccountsRepositorySpec extends Specification {

  //TODO - resolved~~~
//  "retrieve a count of Active loginAccounts that haven't ran since a certain date" in new context {
//    import com.rwgs.scalapostgres.tutorial.persistence.models.AggregationType.ONLINE_BANKING
//
//    insertLoginDetailsTestQuery(testLoginDetails01)
//    insertLoginDetailsTestQuery(testLoginDetails04)
//    insertLoginDetailsTestQuery(testLoginDetails05)
//
//    val count: Int =
//      loginAccountsRepo.
//        getCountOfActiveLoginAccountsByAccountStatusLastUpdated(
//          AccountStatus.NORMAL,
//          DateTime.now.minusDays(1),
//          List(ONLINE_BANKING)
//        ).unsafeRunSync()
//
//    count must be_==(1)  //see why "java.lang.Exception: 0 != 1"??? - RESOLVED!!!
//  }

  //TODO - Error 2:
//  "get count of failed login accounts with no retries left" in new context {
//    insertLoginDetailsTestQuery(testLoginDetails01)
//    insertLoginDetailsTestQuery(testLoginDetails04)
//    insertLoginDetailsTestQuery(testLoginDetails05)
//    insertLoginDetailsTestQuery(testLoginDetails08)
//    insertLoginDetailsTestQuery(testLoginDetails09)
//    insertLoginDetailsTestQuery(testLoginDetails10)
//    insertLoginDetailsTestQuery(testLoginDetails11)
//    insertLoginDetailsTestQuery(testLoginDetails12)
//    insertLoginDetailsTestQuery(firstPrimaryLogin)
//    insertLoginDetailsTestQuery(latestPrimaryLogin)
//
//    /*
//    loginDetailsRepository.getCountOfActiveLoginAccountsByAccountStatusLastUpdated(
//    AccountStatus.BATCH_FAILURE,
//    DateTime.now,
//    List(CORE, NETTELLER)
//    ).unsafeRunSync must_== 3
//     */
//    loginAccountsRepo.
//      getCountOfActiveLoginAccountsByAccountStatusLastUpdated(
//        AccountStatus.BATCH_FAILURE,
//        DateTime.now,
//        List(CORE, NETTELLER)
//      ).unsafeRunSync must_== 3       //see why java.lang.Exception: 0 != 3
//
//    loginAccountsRepo.
//      getCountOfActiveLoginAccountsByAccountStatusLastUpdated(
//        AccountStatus.BATCH_FAILURE,
//        DateTime.now,
//        List(CORE, NETTELLER),
//        fixed = Some(false)
//      ).unsafeRunSync must_== 2
//  }
  import DSO._

  val status = AccountStatus.ONLINE_FAILURE
  val changedEarlierThan = DateTime.now.minusHours(1)
  val beforeCutOffTime = changedEarlierThan.minusMinutes(10)
  val afterCutOffTime = changedEarlierThan.plusMinutes(10)
  val aggTypeNetteller = NETTELLER
  val active = true

  //TODO - Error 3:
  //    java.lang.Exception: 0 != 1
  "check in failed login accounts" in new context {
    val login = createLoginsWithCustomFields(
      status,
      beforeCutOffTime,
      active,
      aggTypeNetteller,
      IsNotDSOBillPayLogin
    )

    insertLoginDetailsTestQuery(login).unsafeRunSync()

    val count: IO[Int] =
      loginAccountsRepo.
        checkInActiveLoginAccountsByAccountStatusLastUpdated(
          status,
          changedEarlierThan,
          List(aggTypeNetteller)
        )

    /*
    !!!!!!!!!!!!! status.id = 6
    !!!!!!!!!!!!! dbDateFormatter.print(date) = 2021-03-24T18:19:19.954Z
    !!!!!!!!!!!!! iAggregationTypes = List(2)
     */
    count.unsafeRunSync ==== 1
  }

////////////////////////////////////////////////////////////////////////////////////////////////
  /*
  //TODO - Error 3 (getCountOfActiveLoginAccountsByAccountStatusLastUpdated):
     //    java.lang.Exception: 0 != 3
    "get count of failed login accounts with no retries left" in new context {
      doobieLoginDetailsRepository.
        getCountOfActiveLoginAccountsByAccountStatusLastUpdated(
          AccountStatus.BATCH_FAILURE,
          DateTime.now,
          List(CORE, NETTELLER)
        ).unsafeRunSync must_== 3

      doobieLoginDetailsRepository.
        getCountOfActiveLoginAccountsByAccountStatusLastUpdated(
          AccountStatus.BATCH_FAILURE,
          DateTime.now,
          List(CORE, NETTELLER),
          fixed = Some(false)
        ).unsafeRunSync must_== 2
    }
   */
////////////////////////////////////////////////////////////////////////////////////////////////


  import org.specs2.matcher.Scope
  trait context extends Scope {
    import cats.syntax.all._                 //needed for ".show" call
    import LoginAccountsQueries._

    import scala.concurrent.ExecutionContext //needed for "Cannot find an implicit value for ContextShift[cats.effect.IO]"
    implicit val cs: ContextShift[IO] = IO.contextShift(ExecutionContext.global)

    import doobie.Transactor
    //TODO - for Transactor, ask:
    // 1) Transactor.fromDriverManager() ???
    // 2) or use "Transactor.fromDataSource"???
    lazy val transactor: Transactor[IO] = Transactor.fromDriverManager[IO](
      "org.postgresql.Driver",
      "jdbc:postgresql://localhost:5432/postgres",
      "postgres",
      "ahs2007"
    )


//    val testUserIdGen: UserId = UserId( FUUID.randomFUUID[IO].unsafeRunSync() )

//    val primaryLoginId = LoginId(FUUID.randomFUUID[IO].unsafeRunSync())
//    val loginId = LoginId(FUUID.randomFUUID[IO].unsafeRunSync())
//    val loginId2 = LoginId(FUUID.randomFUUID[IO].unsafeRunSync())
//    val primaryLoginAccount = makeLogin().copy(loginId = primaryLoginId.show, userId = testUserIdGen, primaryLogin = true)

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
        userId = uuidToString(),
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
        userId = uuidToString(),
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
  val IsDSOBillPayLogin = Value
  val IsNotDSOBillPayLogin = Value
}
