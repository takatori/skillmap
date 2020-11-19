package skillmap.presentation.route

import io.circe.generic.auto._
import org.http4s.HttpRoutes
import skillmap.domain.failure.{ApplicationError, NotFoundFailure, ValidationFailure}
import skillmap.domain.user.User
import skillmap.presentation.response.{BadRequestResponse, ErrorResponse, InternalServerErrorResponse, NotFoundResponse}
import skillmap.usecase.user.UserUseCase.UserUseCase
import sttp.model.StatusCode
import sttp.tapir.Endpoint
import sttp.tapir.json.circe.jsonBody
import sttp.tapir.ztapir.{endpoint, oneOf, statusMapping, _}
import zio.{Task, ZIO}

trait Route[R0] {

  val route: ZIO[R0, Nothing, HttpRoutes[Task]]
  val endpoints: Seq[Endpoint[_, _, _, _]]

  def errorToResponse[R, A](zio: ZIO[R, ApplicationError, A]): ZIO[R, ErrorResponse, A] =
    zio.sandbox.mapError(c =>
      c.failureOrCause match {
        case Left(_) => InternalServerErrorResponse("internal server error")
        case Right(value) =>
          value.squash match {
            case e: NotFoundFailure   => NotFoundResponse(e.message)
            case e: ValidationFailure => BadRequestResponse(e.message)
            case _                    => InternalServerErrorResponse("internal server error")
          }
      }
    )

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
          StatusCode.BadRequest,
          jsonBody[BadRequestResponse].description("invalid request")
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
      .zServerLogicForCurrent { token => ZIO.accessM[UserUseCase](_.get.auth(token).orElseFail(NotFoundResponse(""))) }

}
