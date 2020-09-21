package skillmap.presentation.route.skill

import io.circe.generic.auto._
import skillmap.domain.skill.SkillId
import skillmap.domain.user.User
import skillmap.presentation.response.ErrorResponse
import skillmap.presentation.route.Route
import skillmap.presentation.route.skill.form.SkillForm
import skillmap.presentation.route.skill.response.SkillResponse
import skillmap.usecase.user.UserUseCase
import sttp.tapir.Endpoint
import sttp.tapir.ztapir.{path, _}
import sttp.tapir.json.circe.jsonBody

object SkillRoute {

  object Endpoints {

    private val skillEndpoint       = Route.baseEndpoint.in("skill")
    private val secureSkillEndpoint = Route.secureEndpoint.in("skill")

    val getSkillEndpoint: Endpoint[SkillId, ErrorResponse, SkillResponse, Nothing] =
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
