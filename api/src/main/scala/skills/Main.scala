package skills

import org.http4s.implicits._
import org.http4s.server.Router
import org.http4s.server.blaze.BlazeServerBuilder
import skills.domain.user.{LiveUserRepository, UserRepository}
import skills.presentation.UserRoute
import skills.usecase.UserUseCase
import zio._
import zio.blocking.Blocking
import zio.clock.Clock
import zio.interop.catz._
import zio.interop.catz.implicits._

object Main extends App {

  type AppEnvironment = Clock with UserRepository

  override def run(args: List[String]): URIO[ZEnv, ExitCode] = {

    val result: ZIO[Any, Any, Unit] =
      (for {
        route <- ZIO.accessM[Has[UserRoute.Service]](_.get.route)
        httpApp = Router("/" -> route)
        server <- ZIO.runtime.flatMap { implicit runtime: Runtime[Any] =>
          BlazeServerBuilder[Task](runtime.platform.executor.asEC)
            .bindHttp(8080, "localhost")
            .withHttpApp(httpApp.orNotFound)
            .serve
            .compile
            .drain
        }
      } yield server).provideLayer(Blocking.live >>> LiveUserRepository.live >>> UserUseCase.live >>> UserRoute.live)

    result.exitCode
  }
}
