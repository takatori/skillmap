package skillmap

import org.http4s.HttpRoutes
import org.http4s.implicits._
import org.http4s.server.Router
import org.http4s.server.blaze.BlazeServerBuilder
import skillmap.domain.skill.SkillRepository
import skillmap.domain.user.LiveUserRepository
import skillmap.infrastructure.id.IdFactory
import skillmap.presentation.route.ApiDocRoute
import skillmap.presentation.route.skill.SkillRoute
import skillmap.presentation.route.user.UserRoute
import skillmap.usecase.skill.SkillUseCase
import skillmap.usecase.user.UserUseCase
import zio._
import zio.blocking.Blocking
import zio.interop.catz._
import zio.interop.catz.implicits._

object Main extends App {

  override def run(args: List[String]): URIO[ZEnv, ExitCode] = {

    val userRepository    = Blocking.live >>> LiveUserRepository.live
    val userUseCaseLayer  = (userRepository ++ IdFactory.live) >>> UserUseCase.live
    val skillUseCaseLayer = (SkillRepository.live ++ IdFactory.live) >>> SkillUseCase.live
    val skillRoute        = SkillRoute.route.provideLayer(skillUseCaseLayer ++ userUseCaseLayer)
    val userRoute         = UserRoute.route.provideLayer(userUseCaseLayer)

    import cats.implicits._
    val routeAll: ZIO[Any, Any, HttpRoutes[Task]] =
      ZIO.reduceAll(ApiDocRoute.route, List(userRoute, skillRoute, ApiDocRoute.route))(_ <+> _)

    val result: ZIO[Any, Any, Unit] =
      for {
        r <- routeAll
        httpApp = Router("/" -> r)
        server <- ZIO.runtime.flatMap { implicit runtime: Runtime[Any] =>
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
