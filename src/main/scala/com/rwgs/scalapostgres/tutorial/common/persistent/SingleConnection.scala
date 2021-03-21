package com.rwgs.scalapostgres.tutorial.common.persistent

import cats.effect.{Async, Blocker, ContextShift}
import doobie.util.ExecutionContexts
import doobie.util.transactor.Transactor
import doobie.util.transactor.Transactor.Aux

object SingleConnection {

  def xa[F[_]](implicit cs: ContextShift[F], async: Async[F]): Aux[F, Unit] =
    Transactor.fromDriverManager[F](
      driver = "org.postgresql.Driver",
      url = "jdbc:postgresql://localhost:5432/postgres",
      user = "postgres",
      pass = "ahs2007",
      blocker = Blocker.liftExecutionContext(ExecutionContexts.synchronous)
    )

}
