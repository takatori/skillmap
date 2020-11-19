package skillmap.usecase.skill

import skillmap.domain.skill.{Skill, SkillId, SkillRepository}
import skillmap.infrastructure.id.IdFactory
import zio.test.Assertion._
import zio.test._
import zio.test.mock.Expectation._
import zio.test.mock.mockable

@mockable[SkillRepository.Service]
object MockSkillRepository

object SkillUseCaseSpec extends DefaultRunnableSpec {

  def spec =
    suite("`SkillUseCase.get` must")(
      testM("return `Skill`") {
        val id             = SkillId("test")
        val testSkill      = Skill(id, "test skill", None)
        val mockRepository = MockSkillRepository.Get(equalTo(id), value(Some(testSkill)))
        val layer          = (IdFactory.test ++ mockRepository) >>> SkillUseCase.live
        for {
          result <- SkillUseCase.get(id).provideLayer(layer)
        } yield assert(result)(equalTo(testSkill))
      }
    )
}
