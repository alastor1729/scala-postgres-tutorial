package com.rwgs.scalapostgres.tutorial.persistence

import com.rwgs.scalapostgres.tutorial.domain.User
import doobie.free.connection.ConnectionIO

trait UserRepository[F[_]] {
  def add(u: User): F[Unit]
  def deactivateUser(userId: BigDecimal): ConnectionIO[Unit]
  def find(id: BigDecimal): F[Option[User]]
}
