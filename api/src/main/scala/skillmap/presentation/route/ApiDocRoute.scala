package skillmap.presentation.route

import org.http4s.HttpRoutes
import skillmap.presentation.route.user.UserRoute
import sttp.tapir.swagger.http4s.SwaggerHttp4s
import zio.{Task, UIO, ZIO}
import zio.interop.catz._

object ApiDocRoute {

  import sttp.tapir.docs.openapi._
  import sttp.tapir.openapi.circe.yaml._

  private val yaml = UserRoute.Endpoints.endpoints
    .toOpenAPI("", "")
    .toYaml

  val route: UIO[HttpRoutes[Task]] = ZIO.succeed(new SwaggerHttp4s(yaml).routes[Task])

}
