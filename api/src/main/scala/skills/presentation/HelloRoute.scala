package skills.presentation

import org.http4s._
import sttp.tapir.server.http4s.ztapir._
import sttp.tapir.ztapir._
import zio.{IO, Task}
import zio.interop.catz._
import zio.interop.catz.implicits._

object HelloRoute {

  case class User(name: String)
  val AuthenticationErrorCode = 1001
  def auth(token: String): IO[Int, User] = {
    if (token == "secret") IO.succeed(User("Spock"))
    else IO.fail(AuthenticationErrorCode)
  }

  private val secureEndpoint: ZPartialServerEndpoint[Any, User, Unit, Int, Unit] =
    endpoint
      .in(header[String]("X-AUTH-TOKEN"))
      .errorOut(plainBody[Int])
      .zServerLogicForCurrent(auth)

  private val secureHelloWorldWithLogic =
    secureEndpoint.get
      .in("hello1")
      .in(query[String]("salutation"))
      .out(stringBody)
      .serverLogic {
        case (user, salutation) => IO.succeed(s"$salutation, ${user.name}!")
      }

  private val secureHelloWorld2: ZEndpoint[(String, String), Int, String] = endpoint
    .in(header[String]("X-AUTH-TOKEN"))
    .errorOut(plainBody[Int])
    .get
    .in("hello2")
    .in(query[String]("sautation"))
    .out(stringBody)

  private val secureHelloWorld2WithLogic =
    secureHelloWorld2
      .zServerLogicPart(auth)
      .andThen {
        case (user, salutation) => IO.succeed(s"$salutation, ${user.name}")
      }

  val helloWorldRoutes: HttpRoutes[Task] = List(
    secureHelloWorldWithLogic,
    secureHelloWorld2WithLogic
  ).toRoutes

}
