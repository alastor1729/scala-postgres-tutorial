package com.rwgs.scalapostgres.tutorial.persistence.postgresrepository

import com.rwgs.scalapostgres.tutorial.domain.User
import com.rwgs.scalapostgres.tutorial.persistence.UserRepository
import doobie.{FCS, HC}
import doobie.free.connection.ConnectionIO
import doobie.implicits._

class PostgresUserRepository extends UserRepository[ConnectionIO] {

  override def find(id: BigDecimal): ConnectionIO[Option[User]] =
    PostgresUserRepository.select(id).option

  override def add(u: User): ConnectionIO[Unit] =
    PostgresUserRepository.add(u).run.map(_ => ())

  override def deactivateUser(userId: BigDecimal): ConnectionIO[Unit] =
    PostgresUserRepository.deactivate(userId)

}

object PostgresUserRepository {

  def select(id: BigDecimal): doobie.Query0[User] =
    sql"""
         | SELECT id, name, email, active FROM  "user" WHERE id = $id
         |""".stripMargin.query[User]

  def add(u: User): doobie.Update0 =
    sql"""
         | INSERT INTO "user" (id, name, email, active) VALUES (${u.id}, ${u.name}, ${u.email}, ${u.active})
         |""".stripMargin.update

  def deactivate(userId: BigDecimal): ConnectionIO[Unit] =
    HC.prepareCall("{ call deactivate_user(?) }") {
      for {
        //_ <- FCS.setInt("id", userId.toInt) //String parameterName, int x -- java.sql.SQLFeatureNotSupportedException: Method org.postgresql.jdbc.PgCallableStatement.setInt(String,int) is not yet implemented.
        _ <- FCS.setInt(1, userId.toInt)
        _ <- FCS.execute
      } yield ()
    }

}
