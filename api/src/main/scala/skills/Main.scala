package skills

import org.http4s._
import org.http4s.server.Router
import org.http4s.server.blaze.BlazeServerBuilder
import org.http4s.syntax.kleisli._
import sttp.tapir.server.http4s.ztapir._
import sttp.tapir.ztapir._
import zio._
import zio.interop.catz._
import zio.interop.catz.implicits._

object Main extends App {

  case class User(name: String)
  val AuthenticationErrorCode = 1001
  def auth(token: String): IO[Int, User] = {
    if (token == "secret") IO.succeed(User("Spock"))
    else IO.fail(AuthenticationErrorCode)
  }

  val secureEndpoint: ZPartialServerEndpoint[Any, User, Unit, Int, Unit] =
    endpoint
      .in(header[String]("X-AUTH-TOKEN"))
      .errorOut(plainBody[Int])
      .zServerLogicForCurrent(auth)

  val secureHelloWorldWithLogic = secureEndpoint.get
    .in("hello1")
    .in(query[String]("salutation"))
    .out(stringBody)
    .serverLogic {
      case (user, salutation) => IO.succeed(s"$salutation, ${user.name}!")
    }

  val secureHelloWorld2: ZEndpoint[(String, String), Int, String] = endpoint
    .in(header[String]("X-AUTH-TOKEN"))
    .errorOut(plainBody[Int])
    .get
    .in("hello2")
    .in(query[String]("sautation"))
    .out(stringBody)

  val secureHelloWorld2WithLogic = secureHelloWorld2
    .zServerLogicPart(auth)
    .andThen {
      case (user, salutation) => IO.succeed(s"$salutation, ${user.name}")
    }

  val helloWorldRoutes
    : HttpRoutes[Task] = List(secureHelloWorldWithLogic).toRoutes

  override def run(args: List[String]): URIO[ZEnv, ExitCode] = {
    val server = ZIO.runtime.flatMap { implicit runtime: Runtime[Any] =>
      BlazeServerBuilder[Task](runtime.platform.executor.asEC)
        .bindHttp(8080, "localhost")
        .withHttpApp(Router("/" -> helloWorldRoutes).orNotFound)
        .serve
        .compile
        .drain
    }
    server.exitCode
  }
}
