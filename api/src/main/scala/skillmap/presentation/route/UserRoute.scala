package skillmap.presentation.route

import io.circe.generic.auto._
import org.http4s.HttpRoutes
import skillmap.domain.failure.ExpectedFailure
import skillmap.domain.user.{User, UserId}
import skillmap.infrastructure.id.IdFactory
import skillmap.presentation.response.{ErrorResponse, InternalServerErrorResponse, NotFoundResponse}
import skillmap.presentation.route.Route.Service
import skillmap.usecase.UserUseCase
import sttp.tapir.json.circe.jsonBody
import sttp.tapir.server.http4s.ztapir._
import sttp.tapir.ztapir.{path, _}
import zio.interop.catz._
import zio.{Task, URIO, ZIO, ZLayer}
import cats.implicits._

case class UserResponse(id: String, name: String)
case class UserForm(name: String)

object UserRoute {

  import Endpoints._
  import Logic._

  val live: ZLayer[UserUseCase, Nothing, Route] =
    ZLayer.fromService { implicit usecase =>
      val routes: URIO[UserUseCase, HttpRoutes[Task]] =
        getUserEndPoint.serverLogic(input => getUserLogic(input._1, input._2)).toRoutesR combineK
        registerUserEndpoint.toRoutesR(registerUserLogic(_))

      new Service {
        override def route: ZIO[UserUseCase, Any, HttpRoutes[Task]] = routes
      }
    }

  object Logic {

    def getUserLogic(user: User, id: UserId)(
        implicit userUseCase: UserUseCase.Service
    ): ZIO[Any, ErrorResponse, UserResponse] =
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
        .register(form.name)
        .catchAll {
          case _ => ZIO.fail(InternalServerErrorResponse("internal server error"))
        }
        .provideLayer(IdFactory.live)
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

  }

}
