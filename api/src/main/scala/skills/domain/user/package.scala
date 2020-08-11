package skills.domain

import skills.domain.failure.ExpectedFailure
import zio.{Has, ZIO}

package object user {
  type UserRepository = Has[UserRepository.Service]

  def get(id: String): ZIO[UserRepository, ExpectedFailure, Option[User]] =
    ZIO.accessM(_.get.get(id))

}
