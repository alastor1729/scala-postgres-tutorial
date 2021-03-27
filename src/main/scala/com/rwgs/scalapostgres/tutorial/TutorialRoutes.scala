package com.rwgs.scalapostgres.tutorial

import cats.effect.Sync
import cats.implicits._
import org.http4s.HttpRoutes
import org.http4s.dsl.Http4sDsl
import org.http4s.circe.CirceEntityCodec._
import com.rwgs.scalapostgres.tutorial.domain.User
import com.rwgs.scalapostgres.tutorial.persistence.UserRepository
import doobie.free.connection.ConnectionIO
import doobie.implicits._
import doobie.util.transactor

class TutorialRoutes[F[_]: Sync](userRepository: UserRepository[ConnectionIO],
                                 xa: transactor.Transactor[F]) extends Http4sDsl[F] {

  object UserId extends QueryParamDecoderMatcher[Int]("id")

  def userRoutes: HttpRoutes[F] =
    HttpRoutes.of[F] {

      case req @ POST -> Root / "user" =>
        for {
          user <- req.as[User]
          _ <- userRepository.add(user).transact(xa)
          res <- Created()
        } yield res

      case GET -> Root / "user" :? UserId(userId) =>
        for {
          userOpt <- userRepository.find(userId).transact(xa)
          res <- userOpt match {
            case Some(v) => Ok(v)
            case None    => NotFound(s"User Not Found By Id $userId")
          }
        } yield res

      case GET -> Root / "deactivated" / "user" :? UserId(userId) =>
        for {
          _ <- userRepository.deactivateUser(userId).transact(xa)
          userOpt <- userRepository.find(userId).transact(xa)
          res <- userOpt match {
            case Some(v) => Ok(v)
            case None    => NotFound(s"User Not Found By Id $userId")
          }
        } yield res

    }

}
