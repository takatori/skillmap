package skills.presentation

import io.circe.generic.auto._
import org.http4s.HttpRoutes
import skills.domain.failure.{DBFailure, ExpectedFailure, NotFoundFailure}
import skills.presentation.response.{ErrorResponse, InternalServerErrorResponse, NotFoundResponse}
import skills.usecase.UserUseCase
import sttp.model.StatusCode
import sttp.tapir.json.circe._
import sttp.tapir.server.http4s.ztapir._
import sttp.tapir.ztapir._
import zio.interop.catz._
import zio.{Has, Task, ZIO, ZLayer}

object UserRoute {

  trait Service {
    def route: ZIO[Any, Any, HttpRoutes[Task]]
  }

  val live: ZLayer[Has[UserUseCase.Service], Nothing, Has[UserRoute.Service]] =
    ZLayer.fromService { usecase =>
      val endpointWithLogic =
        getUserEndPoint.toRoutes(id => usecase.getUser(id).map(_.toString).orElse(ZIO.fail(NotFoundResponse(""))))

      new Service {
        override def route: ZIO[Any, Any, HttpRoutes[Task]] =
          ZIO.succeed(endpointWithLogic)
      }
    }

  /*
  private implicit val customServerOptions: Http4sServerOptions[RIO[R, *]] =
    Http4sServerOptions
      .default[RIO[R, *]]
      .copy(decodeFailureHandler = (request, input, failure) => {
        failure match {
          case Error(_, error) =>
            DecodeFailureHandling.response(jsonBody[BadRequestResponse])(
              BadRequestResponse(error.toString)
            )
          case _ => ServerDefaults.decodeFailureHandler(request, input, failure)
        }
      })*/

  private val getUserEndPoint = endpoint.get
    .in("user" / path[String]("user id"))
    .errorOut(
      oneOf(
        statusMapping(
          StatusCode.InternalServerError,
          jsonBody[InternalServerErrorResponse]
        ),
        statusMapping(
          StatusCode.NotFound,
          jsonBody[NotFoundResponse]
        )
      )
    )
    .out(stringBody)

  private def handleError[A](
      result: ZIO[Any, ExpectedFailure, A]
  ): ZIO[Any, Throwable, Either[ErrorResponse, A]] = {
    result
      .fold(
        {
          case DBFailure(t) =>
            Left(InternalServerErrorResponse("Database BOOM!!!", t.getMessage, t.getStackTrace.toString))
          case NotFoundFailure(message) => Left(NotFoundResponse(message))
        },
        Right(_)
      )
      .foldCause(
        c => Left(InternalServerErrorResponse("Unexpected errors", "", c.squash.getStackTrace.toString)),
        identity
      )
  }
}
