package com.rwgs.scalapostgres.tutorial.persistence
package models

import cats.{Eq, Show}
import io.circe.Decoder
import org.joda.time.DateTime

case class LoginDetails(
   loginId: String,
   institutionId: String,
   status: AccountStatus.Value,
   lastStatusChangeDate: DateTime = DateTime.now,
   isOutOfBand: Boolean = false,
   lastFetchEnding: Option[FetchRequestEnding.Value] = None,
   fixed: Boolean = false,
   createdAt: DateTime = DateTime.now,
   updatedAt: DateTime,
   userId: String,
   active: Boolean,
   primaryLogin: Boolean,
   isBillPay: Boolean,
   aggregationType: AggregationType.Value,
   username: String,
   lastSuccessfulEnding: Option[DateTime] = None,
   customerId: Option[String],
   nettellerId: Option[String],
   memberNumber: Option[String],
   failedRetrievalAttempts: Int,
   firstTimeLoggedIn: Option[DateTime]
  /*
  member_number: Option[String],
  failed_retrieval_attempts: Int,
  first_logged_in_dt: Option[Timestamp]

  ///////////////////////////////////////////////////////////////////////////////////////////

  memberNumber: Option[String],
  failedRetrievalAttempts: Int,
  firstTimeLoggedIn: Option[DateTime]
   */
 ){
  def withStatus(
          newStatus: AccountStatus.Value,
          when: DateTime = DateTime.now
                ) =
    copy(status = newStatus, lastStatusChangeDate = when)
}

import io.chrisdavenport.fuuid.circe._ //need this for "Lazy implicit value of type io.circe.generic.extras.decoding.UnwrappedDecoder[com.rwgs.scalapostgres.tutorial.persistence.models.UserId]"
import io.chrisdavenport.fuuid.FUUID
import io.chrisdavenport.fuuid.http4s.FUUIDVar
import io.circe.generic.extras.semiauto.deriveUnwrappedDecoder

//final case class UserId(value: FUUID) extends AnyVal
//
//object UserId {
//  implicit val eq: Eq[UserId] = Eq.fromUniversalEquals
//  implicit val show: Show[UserId] = Show.show(_.value.show)
//  implicit val decoder: Decoder[UserId] = deriveUnwrappedDecoder[UserId]
//}
//
//object UserIdVar {
//  def unapply(s: String): Option[UserId] =
//    FUUIDVar.unapply(s).map(UserId(_))
//}

object AccountStatus extends Enumeration {
  // DO NOT CHANGE THE NAME OR NUMBER OF THESE, JUST ADD
  val NORMAL = Value(0, "Normal")
  val NEEDS_USER_INTERVENTION = Value(1, "Needs User Intervention")
  val BATCH_FAILURE = Value(2, "Failure")
  val NEEDS_OUTSIDE_INTERVENTION = Value(3, "Needs User Intervention Outside of App")
  val BATCH_AGGREGATION_IN_PROGRESS = Value(4, "Aggregation In Progress")
  val ONLINE_AGGREGATION_IN_PROGRESS = Value(5, "Aggregation In Progress")
  val ONLINE_FAILURE = Value(6, "Failure")
  val BATCH_FAILURE_WITH_RETRIES = Value(7, "Failure with retries remaining") //Unused
  val ONLINE_FAILURE_WITH_RETRIES = Value(8, "Failure with retries remaining")  //Unused
}

object FetchRequestEnding extends Enumeration {
  val WrongUsernamePassword = Value(1)
  val WrongAnswer = Value(2)
  val AccountLocked = Value(3)
  val SiteTrouble = Value(4)
  val SiteDown = Value(5)
  val SetupIncomplete = Value(6)
  val UnexpectedResponse = Value(7)
  val AllRoutesCompleted = Value(8)
  val StateMachineTimeOut = Value(9)
  val UnansweredQuestionFound = Value(10)
  val TooManyWrongAnswer = Value(11)
  val SessionTimedOut = Value(12)
  val TransactionParsingFailure = Value(13)
  val ServerSideFailure = Value(14)
  val CaptchaFailure = Value(15)
  val TransferNotEligible = Value(16)
  val ExtractedValueFailure = Value(17)
  val WrongPasswordAndAnswer = Value(18)
  val ConnectionReset = Value(19)
  val ReadTimedOut = Value(20)
  val PrematureEndOfMessageBody = Value(21)
  val ConnectTimeout = Value(22)
  val NoHttpResponse = Value(23)
  val MalformedSocksServer = Value(24)
  val DecryptionException = Value(25)
  val ClientProtocolException = Value(26)
  val UnknownHostException = Value(27)
  val InvalidNetTellerId = Value(28)
}

object AggregationType extends Enumeration {
  val ONLINE_BANKING = Value(0, "Online Banking")
  val CORE = Value(1, "Core")
  val NETTELLER = Value(2, "NetTeller")
  val SYMXCHANGE = Value(3, "SymXchange")
}
