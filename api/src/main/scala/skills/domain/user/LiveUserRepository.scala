package skills.domain.user

import cats.effect.Blocker
import doobie.h2.H2Transactor
import doobie.implicits._
import doobie.util.query.Query0
import doobie.util.transactor.Transactor
import skills.domain.failure.{DBFailure, ExpectedFailure}
import zio.blocking.Blocking
import zio.{blocking, Managed, Task, ZIO, ZLayer}
import zio.interop.catz._

import scala.concurrent.ExecutionContext

class LiveUserRepository(tnx: Transactor[Task]) extends UserRepository.Service {
  import LiveUserRepository._

  override def get(id: String): ZIO[Any, ExpectedFailure, Option[User]] =
    SQL
      .get(id)
      .option
      .transact(tnx)
      .mapError(t => DBFailure(t))

  override def save(user: User): ZIO[Any, Throwable, Unit] = ZIO.unit

  override def remove(id: String): ZIO[Any, Throwable, Unit] = ZIO.unit
}

object LiveUserRepository {

  object SQL {
    def get(id: String): Query0[User] =
      sql"""SELECT * FROM USERS WHERE ID = $id""".query[User]

    def update = ""
    def delete = ""
  }

  def mkTransactor(
      //conf: DbConfig,
      connectEC: ExecutionContext,
      transactEC: ExecutionContext
  ): Managed[Throwable, LiveUserRepository] = {
    import zio.interop.catz._
    H2Transactor
      .newH2Transactor[Task](
        "localhost",
        "root",
        "",
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
