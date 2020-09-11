package skillmap.presentation

import org.http4s.HttpRoutes
import skillmap.usecase.user.UserUseCase
import zio.{Has, Task, ZIO}

package object route {

  type Route = Has[Route.Service]

  def route: ZIO[Route with UserUseCase, Any, HttpRoutes[Task]] =
    ZIO.accessM(_.get.route)
}
