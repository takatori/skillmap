package skillmap.presentation.route.skill

import io.circe.generic.auto._
import org.http4s.HttpRoutes
import skillmap.domain.skill.SkillId
import skillmap.domain.user.User
import skillmap.presentation.response.ErrorResponse
import skillmap.presentation.route.Route
import skillmap.presentation.route.skill.form.SkillForm
import skillmap.presentation.route.skill.response.SkillResponse
import skillmap.usecase.skill
import skillmap.usecase.skill.SkillUseCase
import skillmap.usecase.user.UserUseCase
import sttp.tapir.json.circe.jsonBody
import sttp.tapir.server.http4s.ztapir._
import sttp.tapir.ztapir.{path, _}
import zio.interop.catz._
import zio.{Task, ZIO}

object SkillRoute {

  import Endpoints._
  import Logic._

  val route: ZIO[UserUseCase with SkillUseCase, Any, HttpRoutes[Task]] =
    List[ZServerEndpoint[UserUseCase with SkillUseCase, _, ErrorResponse, _]](
      getSkillEndpoint.zServerLogic(getSkill),
      registerSkillEndpoint.serverLogic(registerSkill)
    ).toRoutesR

  object Logic {

    def getSkill(input: SkillId): ZIO[SkillUseCase, ErrorResponse, SkillResponse] =
      Route.Logic.errorToResponse(for {
        response <- skill
          .get(input)
          .map(s => SkillResponse(s.id.value, s.name, s.description))
      } yield response)

    def registerSkill(input: (User, SkillForm)): ZIO[SkillUseCase, ErrorResponse, Unit] =
      Route.Logic.errorToResponse(for {
        response <- skill
          .register(input._2.name, input._2.description)
      } yield response)
  }

  object Endpoints {

    private val skillEndpoint       = Route.baseEndpoint.in("skill")
    private val secureSkillEndpoint = Route.secureEndpoint.in("skill")

    val getSkillEndpoint: ZEndpoint[SkillId, ErrorResponse, SkillResponse] =
      skillEndpoint.get
        .in(path[String]("skill id").mapTo(SkillId))
        .out(jsonBody[SkillResponse])

    val registerSkillEndpoint: ZPartialServerEndpoint[UserUseCase, User, SkillForm, ErrorResponse, Unit] =
      secureSkillEndpoint.post
        .in(
          jsonBody[SkillForm]
            .description("Register Skill")
            .example(SkillForm("test", Some("test skill")))
        )

    val endpoints = List(getSkillEndpoint, registerSkillEndpoint.endpoint)
  }

}
