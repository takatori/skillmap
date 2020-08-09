package skills.domain.user

import zio.{Has, ULayer, ZIO, ZLayer}

object UserRepository {
  trait Service {
    def get(id: String): ZIO[Any, Throwable, User]
    def save(user: User): ZIO[Any, Throwable, Nothing]
    def remove(id: String): ZIO[Any, Throwable, Nothing]
  }

  object Service {
    val live: Service = new Service {
      override def get(id: String): ZIO[Any, Throwable, User] = ???

      override def save(user: User): ZIO[Any, Throwable, Nothing] = ???

      override def remove(id: String): ZIO[Any, Throwable, Nothing] = ???
    }
  }

  val live: ULayer[Has[Service]] = ZLayer.succeed(Service.live)
}
