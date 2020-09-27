package skillmap.domain.skill

import skillmap.domain.failure.ExpectedFailure
import zio.{ZIO, ZLayer}

object SkillRepository {
  trait Service {
    def get(id: SkillId): ZIO[Any, ExpectedFailure, Option[Skill]]
    def save(skill: Skill): ZIO[Any, Throwable, Unit]
    def remove(id: SkillId): ZIO[Any, ExpectedFailure, Unit]
  }

  object Service {
    val live = new Service {
      override def get(id: SkillId): ZIO[Any, ExpectedFailure, Option[Skill]] = ZIO.succeed(None)

      override def save(skill: Skill): ZIO[Any, Throwable, Unit] = ZIO.unit

      override def remove(id: SkillId): ZIO[Any, ExpectedFailure, Unit] = ZIO.unit
    }
  }

  val live = ZLayer.succeed(Service.live)
}
