package skillmap.presentation.route.user

import io.circe.generic.auto._
import org.http4s.HttpRoutes
import skillmap.domain.user.{User, UserId}
import skillmap.presentation.response.ErrorResponse
import skillmap.presentation.route.Route
import skillmap.presentation.route.user.form.UserForm
import skillmap.presentation.route.user.response.UserResponse
import skillmap.usecase.user
import skillmap.usecase.user.UserUseCase
import sttp.tapir.json.circe.jsonBody
import sttp.tapir.server.http4s.ztapir._
import sttp.tapir.ztapir.{path, _}
import zio.interop.catz._
import zio.{Task, ZIO}

object UserRoute {

  import Endpoints._
  import Logic._

  val route: ZIO[UserUseCase, Nothing, HttpRoutes[Task]] =
    List(
      getUserEndPoint.serverLogic(getUserLogic),
      registerUserEndpoint.zServerLogic(registerUserLogic)
    ).toRoutesR

  object Logic {

    def getUserLogic(input: (User, UserId)): ZIO[UserUseCase, ErrorResponse, UserResponse] =
      Route.Logic.errorToResponse(for {
        response <- user
          .get(input._2)
          .map(u => UserResponse(u.id.value, u.name))
      } yield response)

    def registerUserLogic(form: UserForm): ZIO[UserUseCase, ErrorResponse, Unit] =
      Route.Logic.errorToResponse(for {
        response <- user.register(form.name)
      } yield response)
  }

  object Endpoints {
    private val userEndpoint       = Route.baseEndpoint.in("user")
    private val secureUserEndpoint = Route.secureEndpoint.in("user")

    val getUserEndPoint: ZPartialServerEndpoint[UserUseCase, User, UserId, ErrorResponse, UserResponse] =
      secureUserEndpoint.get
        .in(path[String]("user id").mapTo(UserId))
        .out(jsonBody[UserResponse])

    val registerUserEndpoint: ZEndpoint[UserForm, ErrorResponse, Unit] =
      userEndpoint.post
        .in(
          jsonBody[UserForm]
            .description("Register User")
            .example(UserForm("Test User Name"))
        )

    val endpoints = List(getUserEndPoint.endpoint, registerUserEndpoint)
  }

}
