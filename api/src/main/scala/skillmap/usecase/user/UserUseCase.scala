package skillmap.usecase.user

import skillmap.domain.failure.{ApplicationError, NotFoundFailure}
import skillmap.domain.user.User.{UserId, UserName}
import skillmap.domain.user.{User, UserRepository}
import skillmap.infrastructure.id.IdFactory
import zio.{Has, ZIO, ZLayer}

object UserUseCase {

  type UserUseCase = Has[UserUseCase.Service]

  trait Service {
    def get(id: UserId): ZIO[Any, NotFoundFailure, User]
    def register(name: UserName): ZIO[Any, Nothing, Unit]
    def remove(id: UserId): ZIO[Any, Nothing, Unit]
    def auth(token: String): ZIO[Any, ApplicationError, User]
  }

  val live: ZLayer[UserRepository with IdFactory, Nothing, UserUseCase] =
    ZLayer.fromServices[UserRepository.Service, IdFactory.Service, UserUseCase.Service] { (repo, idFactory) =>
      new Service {
        override def get(id: UserId): ZIO[Any, NotFoundFailure, User] =
          for {
            userOpt <- repo.get(id).orDie
            user    <- ZIO.fromOption(userOpt).orElseFail(NotFoundFailure(s"user($id) not found."))
          } yield user

        override def register(name: UserName): ZIO[Any, Nothing, Unit] =
          for {
            uuid   <- idFactory.generate()
            userId <- ZIO.fromEither(UserId(uuid)).mapError(s => new Throwable(s)).orDie // TODO: fix
            user = User(userId, name)
            result <- repo.save(user).orDie
          } yield result

        override def remove(id: UserId): ZIO[Any, Nothing, Unit] =
          for {
            result <- repo.remove(id).orDie
          } yield result

        override def auth(token: String): ZIO[Any, ApplicationError, User] = ???
        //if (token == "secret") ZIO.succeed(User(UserId("1234")., UserName("Spock").right.get))
        //else ZIO.fail(AuthenticationFailure("error auth"))
      }
    }

  def get(id: UserId): ZIO[UserUseCase, ApplicationError, User] =
    ZIO.accessM(_.get.get(id))

  def register(name: UserName): ZIO[UserUseCase, ApplicationError, Unit] =
    ZIO.accessM(_.get.register(name))

  def remove(id: UserId): ZIO[UserUseCase, ApplicationError, Unit] =
    ZIO.accessM(_.get.remove(id))

  def auth(token: String): ZIO[UserUseCase, ApplicationError, User] =
    ZIO.accessM(_.get.auth(token))

}
