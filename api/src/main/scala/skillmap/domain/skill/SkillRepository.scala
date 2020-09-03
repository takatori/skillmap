package skillmap.domain.skill

import skillmap.domain.failure.ExpectedFailure
import zio.ZIO

object SkillRepository {
  trait Service {
    def get(id: SkillId): ZIO[Any, ExpectedFailure, Option[Skill]]
    def save(skill: Skill): ZIO[Any, Throwable, Unit]
  }
}
