package skills.domain

import zio.{Has, ZIO}

package object user {
  type UserRepository = Has[UserRepository.Service]

  def get(id: String): ZIO[UserRepository, Throwable, User] =
    ZIO.accessM(_.get.get(id))

}
