package skillmap.domain.user

import cats.effect.Blocker
import com.zaxxer.hikari.HikariConfig
import doobie.hikari.HikariTransactor
import doobie.implicits._
import doobie.util.query.Query0
import doobie.util.transactor.Transactor
import skillmap.domain.user.User.UserId
import zio.blocking.Blocking
import zio.interop.catz._
import zio._

import scala.concurrent.ExecutionContext

object CoercibleDoobieCodec {
  import cats.Eq
  import doobie.{Put, Read}
  import io.estatico.newtype.Coercible

  implicit def coerciblePut[R, N](implicit ev: Coercible[Put[R], Put[N]], R: Put[R]): Put[N]      = ev(R)
  implicit def coercibleRead[R, N](implicit ev: Coercible[Read[R], Read[N]], R: Read[R]): Read[N] = ev(R)
  implicit def coercibleEq[R, N](implicit ev: Coercible[Eq[R], Eq[N]], R: Eq[R]): Eq[N]           = ev(R)
}

class LiveUserRepository(tnx: Transactor[Task]) extends UserRepository.Service {

  import LiveUserRepository._

  override def get(id: UserId): ZIO[Any, Throwable, Option[User]] =
    SQL
      .get(id.value.value)
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
      .delete(id.value.value)
      .run
      .transact(tnx)
      .foldM(err => Task.fail(err), _ => ZIO.unit)
}

object LiveUserRepository {
  import CoercibleDoobieCodec._
  import doobie.refined.implicits._
  object SQL {
    def get(id: String): Query0[User] =
      sql"""SELECT * FROM users WHERE `user_id` = $id""".query[User]

    def save(user: User) =
      sql"""INSERT INTO users (
             user_id, 
             name
           ) 
           VALUES (
             ${user.id.value.value}, 
             ${user.name.value.value}
           )
           ON DUPLICATE KEY UPDATE
             name = ${user.name.value.value} 
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
