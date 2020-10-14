package skillmap.domain.user

import cats.effect.Blocker
import com.zaxxer.hikari.HikariConfig
import doobie.hikari.HikariTransactor
import doobie.implicits._
import doobie.util.query.Query0
import doobie.util.transactor.Transactor
import zio.blocking.Blocking
import zio.interop.catz._
import zio._

import scala.concurrent.ExecutionContext

class LiveUserRepository(tnx: Transactor[Task]) extends UserRepository.Service {
  import LiveUserRepository._

  override def get(id: UserId): ZIO[Any, Throwable, Option[User]] =
    SQL
      .get(id.value)
      .option
      .transact(tnx)

  override def save(user: User): ZIO[Any, Throwable, Unit] =
    SQL
      .save(user)
      .run
      .transact(tnx)
      .foldM(err => {
        println(err.getMessage)
        Task.fail(err)
      }, _ => ZIO.unit)

  override def remove(id: UserId): ZIO[Any, Throwable, Unit] =
    SQL
      .delete(id.value)
      .run
      .transact(tnx)
      .foldM(err => Task.fail(err), _ => ZIO.unit)
}

object LiveUserRepository {

  object SQL {
    def get(id: String): Query0[User] =
      sql"""SELECT * FROM users WHERE `user_id` = $id""".query[User]

    def save(user: User) =
      sql"""INSERT INTO users (
             user_id, 
             name
           ) 
           VALUES (
             ${user.id.value}, 
             ${user.name}
           )
           ON DUPLICATE KEY UPDATE
             name = ${user.name} 
           """.update

    def delete(id: String) =
      sql"DELETE FROM users WHERE `user_id` = $id".update
  }

  def mkTransactor(
      //conf: DbConfig,
      connectEC: ExecutionContext,
      transactEC: ExecutionContext
  ): Managed[Throwable, LiveUserRepository] = {
    import zio.interop.catz._

    val config = new HikariConfig()
    config.setDriverClassName("com.mysql.jdbc.Driver")
    config.setJdbcUrl("jdbc:mysql://127.0.0.1:3306/skillmap")
    config.setUsername("root")
    config.setPassword("example")

    HikariTransactor
      .fromHikariConfig[Task](
        config,
        connectEC,
        Blocker.liftExecutionContext(transactEC)
      )
      .toManagedZIO
      .map(new LiveUserRepository(_))
  }

  val live: ZLayer[Blocking, Throwable, UserRepository] =
    ZLayer.fromManaged(for {
      connectEC  <- ZIO.descriptor.map(_.executor.asEC).toManaged_
      blockingEC <- blocking.blocking { ZIO.descriptor.map(_.executor.asEC) }.toManaged_
      managed    <- mkTransactor(connectEC, blockingEC)
    } yield managed)
}
