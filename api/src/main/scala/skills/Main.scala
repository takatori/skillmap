package skills

import org.http4s.server.Router
import org.http4s.server.blaze.BlazeServerBuilder
import org.http4s.syntax.kleisli._
import skills.domain.user.UserRepository
import skills.presentation.{HelloRoute, UserRoute}
import zio._
import zio.clock.Clock
import zio.interop.catz._
import zio.interop.catz.implicits._

object Main extends App {

  type AppEnvironment = Clock with UserRepository
  private val userRoute = UserRoute.userRoute
  private val httpApp   = Router("/" -> userRoute)

  override def run(args: List[String]): URIO[ZEnv, ExitCode] = {
    val server = ZIO.runtime.flatMap { implicit runtime: Runtime[Any] =>
      BlazeServerBuilder[Task](runtime.platform.executor.asEC)
        .bindHttp(8080, "localhost")
        .withHttpApp(httpApp.orNotFound)
        .serve
        .compile
        .drain
    }
    server.exitCode
  }
}
