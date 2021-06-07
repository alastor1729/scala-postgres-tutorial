package com.rwgs.scalapostgres.tutorial

import cats.effect.{ExitCode, IO, IOApp, Resource}
import cats.implicits._
import com.rwgs.scalapostgres.tutorial.common.persistent.HikariCpConnectionPool
import doobie.hikari.HikariTransactor

object Main extends IOApp {

  val pgResource: Resource[IO, HikariTransactor[IO]] =
    HikariCpConnectionPool.loadTransactor(
      "org.postgresql.Driver",
      "jdbc:postgresql://localhost:5432/postgres",
      "postgres",
      "password"
    )

  def run(args: List[String]): IO[ExitCode] =
    pgResource.use { xa =>
      TutorialServer.stream[IO](xa).compile.drain.as(ExitCode.Success)
    }

}
