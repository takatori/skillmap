package skillmap.infrastructure.id

import net.petitviolet.ulid4s.ULID
import zio.{Has, UIO, ULayer, URLayer, ZIO, ZLayer}

object IdFactory {
  trait Service {
    def generate(): UIO[String]
  }

  val uuid: URLayer[Any, Has[IdFactory.Service]] = ZLayer.succeed {
    new Service {
      override def generate(): UIO[String] =
        ZIO.succeed(java.util.UUID.randomUUID.toString.replace("-", ""))
    }
  }

  val ulid: URLayer[Any, Has[IdFactory.Service]] = ZLayer.succeed {
    new Service {
      override def generate(): UIO[String] = ZIO.succeed(ULID.generate)
    }
  }

  val test: ULayer[Has[Service]] = ZLayer.succeed {
    new Service {
      override def generate(): UIO[String] = ZIO.succeed("test value")
    }
  }

}
