package skillmap.usecase.skill

import skillmap.domain.skill.SkillRepository
import zio.test._
import zio.test.mock.mockable

@mockable[SkillRepository.Service]
object MockSkillRepository

object SkillUseCaseSpec extends DefaultRunnableSpec {

  def spec =
    suite("`SkillUseCase.get` must")(
      testM("") {}
    )
}
