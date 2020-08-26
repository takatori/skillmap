package skills.presentation.route

import org.http4s.HttpRoutes
import skills.presentation.response.{ErrorResponse, InternalServerErrorResponse, NotFoundResponse}
import skills.presentation.route.Route.Service
import skills.usecase.UserUseCase
import sttp.model.StatusCode
import sttp.tapir.json.circe.jsonBody
import sttp.tapir.ztapir.{endpoint, oneOf, path, statusMapping}
import zio.{Task, ZIO, ZLayer}
import sttp.tapir.server.http4s.ztapir._
import sttp.tapir.ztapir._
import io.circe.generic.auto._
import skills.domain.failure.ExpectedFailure
import skills.domain.user.{User, UserId}
import sttp.tapir.Endpoint
import zio.interop.catz._
import cats.implicits._

case class UserResponse(id: String, name: String)
case class UserForm(name: String)

object UserRoute {

  import Endpoints._
  import Logics._

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

  object Logics {

    def getUserLogic(id: String)(implicit userUseCase: UserUseCase.Service): ZIO[Any, ErrorResponse, UserResponse] =
      userUseCase
        .get(new UserId(id))
        .map(u => UserResponse(u.id.value, u.name))
        .catchAll {
          case _: ExpectedFailure => ZIO.fail(NotFoundResponse(s"user not found $id."))
          case e                  => ZIO.fail(InternalServerErrorResponse(s"$e", "", ""))
        }

    def registerUserLogic(
        form: UserForm
    )(implicit userUseCase: UserUseCase.Service): ZIO[Any, InternalServerErrorResponse, Unit] =
      userUseCase
        .save(User(new UserId("1234"), form.name))
        .catchAll {
          case e => ZIO.fail(InternalServerErrorResponse(s"$e", "", s"${e.getMessage}"))
        }
  }

  object Endpoints {
    val userEndpoint: Endpoint[Unit, ErrorResponse, Unit, Nothing] = endpoint
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
      .in("user")

    val getUserEndPoint: Endpoint[String, ErrorResponse, UserResponse, Nothing] = userEndpoint.get
      .in(path[String]("user id"))
      .out(jsonBody[UserResponse])

    val registerUserEndpoint: Endpoint[UserForm, ErrorResponse, Unit, Nothing] = userEndpoint.post
      .in(
        jsonBody[UserForm]
          .description("Register User")
          .example(UserForm("Test User Name"))
      )

  }

}
