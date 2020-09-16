package skillmap.presentation.route

import io.circe.generic.auto._
import org.http4s.HttpRoutes
import skillmap.domain.failure.ExpectedFailure
import skillmap.domain.user.{User, UserId}
import skillmap.infrastructure.id.IdFactory
import skillmap.presentation.response.{ErrorResponse, InternalServerErrorResponse, NotFoundResponse}
import skillmap.usecase.UserUseCase
import sttp.tapir.json.circe.jsonBody
import sttp.tapir.server.http4s.ztapir._
import sttp.tapir.ztapir.{path, _}
import zio.interop.catz._
import zio.{Task, ZIO}
import cats.implicits._

case class UserResponse(id: String, name: String)
case class UserForm(name: String)

object UserRoute {

  import Endpoints._
  import Logic._

  val route: ZIO[UserUseCase, Nothing, HttpRoutes[Task]] =
    ZIO.mapN(
      getUserEndPoint.serverLogic(getUserLogic).toRoutesR,
      registerUserEndpoint.zServerLogic(registerUserLogic).toRoutesR
    )(_ <+> _)

  object Logic {

    def getUserLogic(input: (User, UserId)): ZIO[UserUseCase, ErrorResponse, UserResponse] =
      for {
        response <- ZIO.accessM[UserUseCase](
          _.get
            .get(input._2)
            .map(u => UserResponse(u.id.value, u.name))
            .catchAll {
              case _: ExpectedFailure => ZIO.fail(NotFoundResponse(s"user not found ${input._2}."))
              case _                  => ZIO.fail(InternalServerErrorResponse(s"internal server error"))
            }
        )
      } yield response

    def registerUserLogic(form: UserForm): ZIO[UserUseCase, InternalServerErrorResponse, Unit] =
      for {
        response <- ZIO.accessM[UserUseCase](
          _.get
            .register(form.name)
            .catchAll {
              case _ => ZIO.fail(InternalServerErrorResponse("internal server error"))
            }
            .provideLayer(IdFactory.live)
        )
      } yield response
  }

  object Endpoints {

    import Route.Service._

    private val userEndpoint = baseEndpoint.in("user")

    private val secureUserEndpoint: ZPartialServerEndpoint[UserUseCase, User, Unit, ErrorResponse, Unit] =
      secureEndpoint.in("user")

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
