package skillmap

import org.http4s.implicits._
import org.http4s.server.Router
import org.http4s.server.blaze.BlazeServerBuilder
import skillmap.domain.user.{LiveUserRepository, UserRepository}
import skillmap.presentation.route.UserRoute.Endpoints
import sttp.tapir.swagger.http4s.SwaggerHttp4s
import zio._
import zio.blocking.Blocking
import zio.clock.Clock
import zio.interop.catz._
import zio.interop.catz.implicits._
import cats.implicits._
import skillmap.presentation.route.Route
import skillmap.usecase.user.UserUseCase

object Main extends App {

  type AppEnvironment = Clock with UserRepository

  override def run(args: List[String]): URIO[ZEnv, ExitCode] = {

    import sttp.tapir.docs.openapi._
    import sttp.tapir.openapi.circe.yaml._
    val yaml =
      List(Endpoints.getUserEndPoint.endpoint, Endpoints.registerUserEndpoint).toOpenAPI("Our pets", "1.0").toYaml

    val result: ZIO[Any, Any, Unit] =
      (for {
        route <- presentation.route.route
        httpApp = Router("/" -> route)
        server <- ZIO.runtime.flatMap { implicit runtime: Runtime[Any] =>
          BlazeServerBuilder[Task](runtime.platform.executor.asEC)
            .bindHttp(8080, "localhost")
            .withHttpApp((httpApp <+> new SwaggerHttp4s(yaml).routes[Task]).orNotFound)
            .serve
            .compile
            .drain
        }
      } yield server).provideLayer(Blocking.live >>> LiveUserRepository.live >>> UserUseCase.live >>> Route.live)

    result.exitCode
  }
}
