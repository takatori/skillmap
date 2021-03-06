package skillmap.domain

import skillmap.domain.user.User.UserId
import zio.{Has, ZIO}

package object user {
  type UserRepository = Has[UserRepository.Service]

  def get(id: UserId): ZIO[UserRepository, Throwable, Option[User]] =
    ZIO.accessM(_.get.get(id))

}
