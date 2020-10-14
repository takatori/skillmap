package skillmap.presentation.route

import io.circe.generic.auto._
import skillmap.domain.failure.{ApplicationError, NotFoundFailure}
import skillmap.domain.user.User
import skillmap.presentation.response.{ErrorResponse, InternalServerErrorResponse, NotFoundResponse}
import skillmap.usecase.user.UserUseCase
import sttp.model.StatusCode
import sttp.tapir.Endpoint
import sttp.tapir.json.circe.jsonBody
import sttp.tapir.ztapir.{endpoint, oneOf, statusMapping, _}
import zio.ZIO

object Route {

  object Logic {
    def errorToResponse[R, A](zio: ZIO[R, ApplicationError, A]): ZIO[R, ErrorResponse, A] =
      zio.sandbox.mapError(c =>
        c.failureOrCause match {
          case Left(_) => InternalServerErrorResponse("internal server error")
          case Right(value) =>
            value.squash match {
              case e: NotFoundFailure => NotFoundResponse(e.message)
              case _                  => InternalServerErrorResponse("internal server error")
            }
        }
      )
  }

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
      .zServerLogicForCurrent { token => ZIO.accessM[UserUseCase](_.get.auth(token).orElseFail(NotFoundResponse(""))) }

}
