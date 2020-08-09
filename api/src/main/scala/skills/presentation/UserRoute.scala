package skills.presentation

import io.circe.generic.auto._
import org.http4s.HttpRoutes
import sttp.tapir.server.http4s.ztapir._
import sttp.tapir.ztapir._
import sttp.tapir.json.circe._
import sttp.model.StatusCode
import zio.{IO, Task, ZIO}
import skills.domain.failure.{DBFailure, ExpectedFailure, NotFoundFailure}
import skills.presentation.response.{ErrorResponse, InternalServerErrorResponse, NotFoundResponse}
import zio.interop.catz._
import zio.interop.catz.implicits._

object UserRoute {

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
    .zServerLogic {
      case (id) => IO.succeed(s"$id")
    }

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

  val userRoute: HttpRoutes[Task] = List(
    getUserEndPoint
  ).toRoutes
}
