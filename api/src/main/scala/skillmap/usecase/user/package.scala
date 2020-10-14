package skillmap.usecase

import skillmap.domain.failure.ApplicationError
import skillmap.domain.user.{User, UserId}
import zio.{Has, ZIO}

package object user {
  type UserUseCase = Has[UserUseCase.Service]

  def get(id: UserId): ZIO[UserUseCase, ApplicationError, User] =
    ZIO.accessM(_.get.get(id))

  def register(name: String): ZIO[UserUseCase, ApplicationError, Unit] =
    ZIO.accessM(_.get.register(name))

  def remove(id: UserId): ZIO[UserUseCase, ApplicationError, Unit] =
    ZIO.accessM(_.get.remove(id))

  def auth(token: String): ZIO[UserUseCase, ApplicationError, User] =
    ZIO.accessM(_.get.auth(token))

}
