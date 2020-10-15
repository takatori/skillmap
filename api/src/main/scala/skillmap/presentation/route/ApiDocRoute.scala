package skillmap.presentation.route

import org.http4s.HttpRoutes
import skillmap.presentation.route.skill.SkillRoute
import skillmap.presentation.route.user.UserRoute
import sttp.tapir.swagger.http4s.SwaggerHttp4s
import zio.interop.catz._
import zio.{Task, ZIO}

object ApiDocRoute extends Route[Any] {

  import sttp.tapir.docs.openapi._
  import sttp.tapir.openapi.circe.yaml._

  override val endpoints =
    UserRoute.endpoints ++
    SkillRoute.endpoints

  private val yaml = endpoints
    .toOpenAPI("skillmap api", "0.0.1")
    .toYaml

  override val route: ZIO[Any, Nothing, HttpRoutes[Task]] = ZIO.succeed(new SwaggerHttp4s(yaml).routes[Task])

}
