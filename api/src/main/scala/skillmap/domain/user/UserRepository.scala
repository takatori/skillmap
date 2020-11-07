package skillmap.domain.user

import skillmap.domain.user.User.UserId
import zio.ZIO

object UserRepository {
  trait Service {
    def get(id: UserId): ZIO[Any, Throwable, Option[User]]
    def save(user: User): ZIO[Any, Throwable, Unit]
    def remove(id: UserId): ZIO[Any, Throwable, Unit]
  }
}
