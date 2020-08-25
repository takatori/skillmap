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
import skills.domain.user.UserId
import zio.interop.catz._

case class UserResponse(id: String, name: String)
case class UserForm(name: String)

object UserRoute {

  import Endpoints._
  import Logics._

  val live: ZLayer[UserUseCase, Nothing, Route] =
    ZLayer.fromService { implicit usecase =>
      val routes: HttpRoutes[Task] = getUserEndPoint.toRoutes(getUserLogic(_))
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

  }

  object Endpoints {
    val userEndpoint = endpoint
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

    val getUserEndPoint = userEndpoint.get
      .in(path[String]("user id"))
      .out(jsonBody[UserResponse])

    val registerUser = userEndpoint.post
      .in(
        jsonBody[UserForm]
          .description("Register User")
          .example(UserForm("Test User Name"))
      )

  }

}
