package skills

import org.http4s.HttpRoutes
import org.http4s.server.Router
import org.http4s.server.blaze.BlazeServerBuilder
import org.http4s.syntax.kleisli._
import skills.domain.user.UserRepository
import skills.presentation.UserRoute
import zio._
import zio.clock.Clock
import zio.interop.catz._
import zio.interop.catz.implicits._
import zio.console.putStrLn

object Main extends App {

  type AppEnvironment = Clock with UserRepository
  private val httpApp: HttpRoutes[Task] = Router("/" -> userRoute)

  override def run(args: List[String]): URIO[ZEnv, ExitCode] = {

    val result: ZIO[Any, Nothing, ZIO[Any, Throwable, Unit]] = for {
      route <- ZIO.accessM(_.route)
      httpApp = Route("/" -> route)
      server = ZIO.runtime.flatMap { implicit runtime: Runtime[Any] =>
        BlazeServerBuilder[Task](runtime.platform.executor.asEC)
          .bindHttp(8080, "localhost")
          .withHttpApp(httpApp.orNotFound)
          .serve
          .compile
          .drain
      }
    } yield server

    result.exitCode
  }
}
