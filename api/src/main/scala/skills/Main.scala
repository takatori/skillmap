package wazamap

import cats.effect.{ExitCode, IO, IOApp}
import cats.syntax.either._
import org.http4s.HttpApp
import org.http4s.implicits._
import org.http4s.server.blaze.BlazeServerBuilder
import sttp.tapir.Endpoint
import sttp.tapir._
import sttp.tapir.server.http4s._
import scala.concurrent.ExecutionContext.global

object Main extends IOApp {

  val helloWorldEP: Endpoint[String, Unit, String, Nothing] =
    endpoint.get.in("hello" / path[String]("name")).out(stringBody)

  def helloLogic(name: String): IO[Either[Unit, String]] = IO {
    s"Hello, $name.".asRight[Unit]
  }

  val helloWorldRoute =
    helloWorldEP toRoutes helloLogic

  val helloWorldService: HttpApp[IO] =
    helloWorldRoute orNotFound

  def run(args: List[String]): IO[ExitCode] =
    BlazeServerBuilder[IO](global)
    .bindHttp(8080, "localhost")
    .withHttpApp(helloWorldService)
    .serve
    .compile
    .drain
    .as(ExitCode.Success)


}