package skills.presentation.route

import org.http4s.HttpRoutes
import skills.presentation.response.{InternalServerErrorResponse, NotFoundResponse}
import skills.presentation.route.Route.Service
import skills.usecase.UserUseCase
import sttp.model.StatusCode
import sttp.tapir.json.circe.jsonBody
import sttp.tapir.ztapir.{endpoint, oneOf, path, statusMapping, stringBody}
import zio.{Task, ZIO, ZLayer}
import sttp.tapir.server.http4s.ztapir._
import sttp.tapir.ztapir._
import io.circe.generic.auto._
import zio.interop.catz._

object UserRoute {

  val live: ZLayer[UserUseCase, Nothing, Route] =
    ZLayer.fromService { usecase =>
      val endpointWithLogic: HttpRoutes[Task] =
        getUserEndPoint.toRoutes(id => usecase.getUser(id).map(_.toString).orElse(ZIO.fail(NotFoundResponse(""))))

      new Service {
        override def route: ZIO[Any, Any, HttpRoutes[Task]] =
          ZIO.succeed(endpointWithLogic)
      }
    }

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

}
