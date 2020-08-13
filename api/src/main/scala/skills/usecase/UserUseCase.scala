package skills.usecase

import skills.domain.failure.{ExpectedFailure, NotFoundFailure}
import skills.domain.user.{User, UserId, UserRepository}
import zio.{ZIO, ZLayer}

object UserUseCase {

  trait Service {
    def getUser(id: UserId): ZIO[Any, ExpectedFailure, User]
  }

  val live: ZLayer[UserRepository, Nothing, UserUseCase] =
    ZLayer.fromService { repo =>
      new Service {
        override def getUser(id: UserId): ZIO[Any, ExpectedFailure, User] =
          for {
            userOpt <- repo.get(id)
            user    <- ZIO.fromOption(userOpt).mapError(_ => NotFoundFailure(s"user($id) is not found."))
          } yield user
      }
    }
}
