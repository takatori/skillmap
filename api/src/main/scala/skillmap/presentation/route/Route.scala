package skillmap.presentation.route

import org.http4s.HttpRoutes
import skillmap.domain.failure.{DBFailure, ExpectedFailure, NotFoundFailure}
import skillmap.presentation.response.{ErrorResponse, InternalServerErrorResponse, NotFoundResponse}
import zio.{Task, ZIO}

object Route {
  trait Service {
    def route: ZIO[Any, Any, HttpRoutes[Task]]
  }

  object Service {
    val live = new Service {
      override def route: ZIO[Any, Any, HttpRoutes[Task]] = ???
    }
  }

  def handleError[A](
      result: ZIO[Any, ExpectedFailure, A]
  ): ZIO[Any, Throwable, Either[ErrorResponse, A]] = {
    result
      .fold(
        {
          case DBFailure(t) =>
            Left(InternalServerErrorResponse("Database BOOM!!!", t.getMessage, t.getStackTrace.toString))
          case NotFoundFailure(message) => Left(NotFoundResponse(message))
        },
        Right(_)
      )
      .foldCause(
        c => Left(InternalServerErrorResponse("Unexpected errors", "", c.squash.getStackTrace.toString)),
        identity
      )
  }

}
