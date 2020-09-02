package skillmap.infrastructure

import zio.{Has, ZIO}

package object id {
  type IdFactory = Has[IdFactory.Service]

  def generate(): ZIO[IdFactory, Nothing, String] =
    ZIO.accessM(_.get.generate())
}
