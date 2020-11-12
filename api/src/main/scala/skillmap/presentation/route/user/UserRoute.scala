package skillmap.presentation.route.user

import io.circe.generic.auto._
import org.http4s.HttpRoutes
import skillmap.domain.failure.ValidationFailure
import skillmap.domain.user.User.{UserId, UserName}
import skillmap.domain.user.User
import skillmap.presentation.response.{BadRequestResponse, ErrorResponse}
import skillmap.presentation.route.Route
import skillmap.presentation.route.user.form.UserForm
import skillmap.presentation.route.user.response.UserResponse
import skillmap.usecase.user
import skillmap.usecase.user.UserUseCase
import sttp.tapir.CodecFormat.TextPlain
import sttp.tapir.{Codec, DecodeResult}
import sttp.tapir.json.circe.jsonBody
import sttp.tapir.server.http4s.ztapir._
import sttp.tapir.ztapir.{path, _}
import zio.interop.catz._
import zio.{Task, ZIO}

object UserRoute extends Route[UserUseCase] {

  import Endpoints._
  import Logic._

  override val route: ZIO[UserUseCase, Nothing, HttpRoutes[Task]] =
    List(
      getUserEndPoint.serverLogic(getUserLogic),
      registerUserEndpoint.zServerLogic(registerUserLogic)
    ).toRoutesR

  override val endpoints = List(getUserEndPoint.endpoint, registerUserEndpoint)

  object Endpoints {
    private val userEndpoint       = baseEndpoint.in("user")
    private val secureUserEndpoint = secureEndpoint.in("user")

    def decode(id: String): DecodeResult[UserId] = UserId(id) match {
      case Right(u) => DecodeResult.Value(u)
      case Left(s)  => DecodeResult.Error(id, new Throwable(s))
    }
    def encode(id: UserId): String = id.value.value

    implicit val myIdCodec: Codec[String, UserId, TextPlain] =
      Codec.string.mapDecode(decode)(encode)

    val getUserEndPoint: ZPartialServerEndpoint[UserUseCase, User, UserId, ErrorResponse, UserResponse] =
      secureUserEndpoint.get
        .in(path[UserId]("user id"))
        .out(jsonBody[UserResponse])

    val registerUserEndpoint: ZEndpoint[UserForm, ErrorResponse, Unit] =
      userEndpoint.post
        .in(
          jsonBody[UserForm]
            .description("Register User")
            .example(UserForm("Test User Name"))
        )
  }

  object Logic {

    def getUserLogic(input: (User, UserId)): ZIO[UserUseCase, ErrorResponse, UserResponse] =
      errorToResponse(for {
        response <- user
          .get(input._2)
          .map(UserResponse.from)
      } yield response)

    def registerUserLogic(form: UserForm): ZIO[UserUseCase, ErrorResponse, Unit] =
      errorToResponse(for {
        userName <- form.validate.mapError(ValidationFailure)
        response <- user.register(userName)
      } yield response)
  }

}
