package skillmap.usecase.user

import skillmap.domain.failure.{AuthenticationFailure, DBFailure, ExpectedFailure, NotFoundFailure}
import skillmap.domain.user.{User, UserId, UserRepository}
import skillmap.infrastructure.id.IdFactory
import zio.{ZIO, ZLayer}

object UserUseCase {

  trait Service {
    def get(id: UserId): ZIO[Any, ExpectedFailure, User]
    def register(name: String): ZIO[Any, ExpectedFailure, Unit]
    def remove(id: UserId): ZIO[Any, ExpectedFailure, Unit]
    def auth(token: String): ZIO[Any, ExpectedFailure, User]
  }

  val live: ZLayer[UserRepository with IdFactory, Nothing, UserUseCase] =
    ZLayer.fromServices[UserRepository.Service, IdFactory.Service, UserUseCase.Service] { (repo, idFactory) =>
      new Service {
        override def get(id: UserId): ZIO[Any, ExpectedFailure, User] =
          for {
            userOpt <- repo.get(id)
            user    <- ZIO.fromOption(userOpt).orElseFail(NotFoundFailure(s"user($id) not found."))
          } yield user

        override def register(name: String): ZIO[Any, ExpectedFailure, Unit] =
          for {
            uid <- idFactory.generate()
            user = User(UserId(uid), name)
            result <- repo.save(user).mapError(e => DBFailure(e))
          } yield result

        override def remove(id: UserId): ZIO[Any, ExpectedFailure, Unit] =
          for {
            result <- repo.remove(id).mapError(e => DBFailure(e))
          } yield result

        override def auth(token: String): ZIO[Any, ExpectedFailure, User] = {
          if (token == "secret") ZIO.succeed(User(UserId("1234"), "Spock"))
          else ZIO.fail(AuthenticationFailure("error auth"))
        }
      }
    }
}
