package skills.domain.user

import skills.domain.failure.ExpectedFailure
import zio.ZIO

object UserRepository {
  trait Service {
    def get(id: String): ZIO[Any, ExpectedFailure, Option[User]]
    def save(user: User): ZIO[Any, Throwable, Unit]
    def remove(id: String): ZIO[Any, Throwable, Unit]
  }
}
