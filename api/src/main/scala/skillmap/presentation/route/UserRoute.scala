package skillmap.presentation.route

import org.http4s.HttpRoutes
import skillmap.presentation.response.{ErrorResponse, InternalServerErrorResponse, NotFoundResponse}
import skillmap.presentation.route.Route.Service
import skillmap.usecase.UserUseCase
import sttp.model.StatusCode
import sttp.tapir.json.circe.jsonBody
import sttp.tapir.ztapir.{endpoint, oneOf, path, statusMapping}
import zio.{Task, ZIO, ZLayer}
import sttp.tapir.server.http4s.ztapir._
import sttp.tapir.ztapir._
import io.circe.generic.auto._
import skillmap.domain.failure.ExpectedFailure
import skillmap.domain.user.{User, UserId}
import sttp.tapir.Endpoint
import zio.interop.catz._
import cats.implicits._

case class UserResponse(id: String, name: String)
case class UserForm(name: String)

object UserRoute {

  import Endpoints._
  import Logic._

  val live: ZLayer[UserUseCase, Nothing, Route] =
    ZLayer.fromService { implicit usecase =>
      val routes: HttpRoutes[Task] =
        getUserEndPoint.toRoutes(getUserLogic(_)) combineK
        registerUserEndpoint.toRoutes(registerUserLogic(_))

      new Service {
        override def route: ZIO[Any, Any, HttpRoutes[Task]] =
          ZIO.succeed(routes)
      }
    }

  object Logic {

    def getUserLogic(id: UserId)(implicit userUseCase: UserUseCase.Service): ZIO[Any, ErrorResponse, UserResponse] =
      userUseCase
        .get(id)
        .map(u => UserResponse(u.id.value, u.name))
        .catchAll {
          case _: ExpectedFailure => ZIO.fail(NotFoundResponse(s"user not found $id."))
          case _                  => ZIO.fail(InternalServerErrorResponse(s"internal server error"))
        }

    def registerUserLogic(
        form: UserForm
    )(implicit userUseCase: UserUseCase.Service): ZIO[Any, InternalServerErrorResponse, Unit] =
      userUseCase
        .save(User(UserId("1234"), form.name))
        .catchAll {
          case _ => ZIO.fail(InternalServerErrorResponse("internal server error"))
        }
  }

  object Endpoints {
    val baseEndpoint: Endpoint[Unit, ErrorResponse, Unit, Nothing] = endpoint
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

    val userEndpoint: Endpoint[Unit, ErrorResponse, Unit, Nothing] = baseEndpoint.in("user")

    val getUserEndPoint: Endpoint[UserId, ErrorResponse, UserResponse, Nothing] =
      userEndpoint.get
        .in(path[String]("user id").mapTo(UserId))
        .out(jsonBody[UserResponse])

    val registerUserEndpoint: Endpoint[UserForm, ErrorResponse, Unit, Nothing] =
      userEndpoint.post
        .in(
          jsonBody[UserForm]
            .description("Register User")
            .example(UserForm("Test User Name"))
        )

  }

}
