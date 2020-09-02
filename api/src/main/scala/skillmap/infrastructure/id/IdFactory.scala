package skillmap.infrastructure.id

import zio.{Has, UIO, URLayer, ZIO, ZLayer}

object IdFactory {
  trait Service {
    def generate(): UIO[String]
  }

  val live: URLayer[Any, Has[IdFactory.Service]] = ZLayer.succeed {
    new Service {
      override def generate(): UIO[String] =
        ZIO.succeed(java.util.UUID.randomUUID.toString.replace("-", ""))
    }
  }
}
