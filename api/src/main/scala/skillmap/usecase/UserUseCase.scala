package skillmap.usecase

import skillmap.domain.failure.{DBFailure, ExpectedFailure, NotFoundFailure}
import skillmap.domain.user.{User, UserId, UserRepository}
import skillmap.infrastructure.id
import skillmap.infrastructure.id.IdFactory
import zio.{ZIO, ZLayer}

object UserUseCase {

  trait Service {
    def get(id: UserId): ZIO[Any, ExpectedFailure, User]
    def register(name: String): ZIO[IdFactory, ExpectedFailure, Unit]
    def remove(id: UserId): ZIO[Any, ExpectedFailure, Unit]
  }

  val live: ZLayer[UserRepository, Nothing, UserUseCase] =
    ZLayer.fromService { repo =>
      new Service {
        override def get(id: UserId): ZIO[Any, ExpectedFailure, User] =
          for {
            userOpt <- repo.get(id)
            user <- ZIO
              .fromOption(userOpt)
              .mapError(_ => NotFoundFailure(s"user($id) not found."))
          } yield user

        override def register(name: String): ZIO[IdFactory, ExpectedFailure, Unit] =
          for {
            uid <- id.generate()
            user = User(UserId(uid), name)
            result <- repo.save(user).mapError(e => DBFailure(e))
          } yield result

        override def remove(id: UserId): ZIO[Any, ExpectedFailure, Unit] =
          for {
            result <- repo.remove(id).mapError(e => DBFailure(e))
          } yield result
      }
    }
}
