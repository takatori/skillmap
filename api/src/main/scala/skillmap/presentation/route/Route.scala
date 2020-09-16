package skillmap.presentation.route

import io.circe.generic.auto._
import org.http4s.HttpRoutes
import skillmap.domain.user.User
import skillmap.presentation.response.{ErrorResponse, InternalServerErrorResponse, NotFoundResponse}
import skillmap.usecase.UserUseCase
import sttp.model.StatusCode
import sttp.tapir.Endpoint
import sttp.tapir.json.circe.jsonBody
import sttp.tapir.ztapir.{endpoint, oneOf, statusMapping, _}
import zio.{Task, ZIO, ZLayer}

object Route {
  trait Service {
    def route: ZIO[UserUseCase, Any, HttpRoutes[Task]]
  }

  object Service {

    val baseEndpoint: Endpoint[Unit, ErrorResponse, Unit, Nothing] = endpoint
      .errorOut(
        oneOf[ErrorResponse](
          statusMapping(
            StatusCode.InternalServerError,
            jsonBody[InternalServerErrorResponse].description("internal server error")
          ),
          statusMapping(
            StatusCode.NotFound,
            jsonBody[NotFoundResponse].description("resource not found")
          ),
          statusMapping(
            StatusCode.Forbidden,
            jsonBody[NotFoundResponse].description("auth error")
          ),
          statusDefaultMapping(jsonBody[InternalServerErrorResponse])
        )
      )

    val secureEndpoint: ZPartialServerEndpoint[UserUseCase, User, Unit, ErrorResponse, Unit] =
      baseEndpoint
        .in(header[String]("X-AUTH-TOKEN"))
        .zServerLogicForCurrent(token =>
          ZIO.accessM[UserUseCase](_.get.auth(token).mapError(_ => NotFoundResponse("")))
        )

  }
  val live: ZLayer[Any, Nothing, Route] =
    ZLayer.succeed(
      new Service {
        import zio.interop.catz._
        import cats.implicits._
        override def route: ZIO[UserUseCase, Any, HttpRoutes[Task]] =
          ZIO.mapN(UserRoute.route, ApiDocRoute.route)(_ <+> _)
      }
    )

}
