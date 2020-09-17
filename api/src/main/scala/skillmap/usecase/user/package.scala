package skillmap.usecase

import skillmap.domain.failure.ExpectedFailure
import skillmap.domain.user.{User, UserId}
import zio.{Has, ZIO}

package object user {
  type UserUseCase = Has[UserUseCase.Service]

  def get(id: UserId): ZIO[UserUseCase, ExpectedFailure, User] =
    ZIO.accessM(_.get.get(id))

  def register(name: String): ZIO[UserUseCase, ExpectedFailure, Unit] =
    ZIO.accessM(_.get.register(name))

  def remove(id: UserId): ZIO[UserUseCase, ExpectedFailure, Unit] =
    ZIO.accessM(_.get.remove(id))

  def auth(token: String): ZIO[UserUseCase, ExpectedFailure, User] =
    ZIO.accessM(_.get.auth(token))

}
