package skillmap.presentation.route

import org.http4s.HttpRoutes
import skillmap.presentation.route.skill.SkillRoute
import skillmap.presentation.route.user.UserRoute
import sttp.tapir.swagger.http4s.SwaggerHttp4s
import zio.interop.catz._
import zio.{Task, ZIO}

object ApiDocRoute {

  import sttp.tapir.docs.openapi._
  import sttp.tapir.openapi.circe.yaml._

  private val endpoints =
    UserRoute.Endpoints.endpoints ++
    SkillRoute.Endpoints.endpoints

  private val yaml = endpoints
    .toOpenAPI("skillmap api", "0.0.1")
    .toYaml

  val route: ZIO[Any, Any, HttpRoutes[Task]] = ZIO.succeed(new SwaggerHttp4s(yaml).routes[Task])

}
