package skills.usecase

import skills.domain.failure.{ExpectedFailure, NotFoundFailure}
import skills.domain.user.{User, UserRepository}
import zio.{Has, ZIO, ZLayer}

object UserUseCase {

  trait Service {
    def getUser(id: String): ZIO[Any, ExpectedFailure, User]
  }

  val live: ZLayer[UserRepository, Nothing, Has[UserUseCase.Service]] =
    ZLayer.fromService { repo =>
      new Service {
        override def getUser(id: String): ZIO[Any, ExpectedFailure, User] =
          for {
            userOpt <- repo.get(id)
            user    <- ZIO.fromOption(userOpt).mapError(_ => NotFoundFailure(s"user($id) is not found."))
          } yield user
      }
    }
}
