package skillmap.domain.skill

import zio.{ZIO, ZLayer}

object SkillRepository {
  trait Service {
    def get(id: SkillId): ZIO[Any, Throwable, Option[Skill]]
    def save(skill: Skill): ZIO[Any, Throwable, Unit]
    def remove(id: SkillId): ZIO[Any, Throwable, Unit]
  }

  object Service {
    val live = new Service {
      override def get(id: SkillId): ZIO[Any, Throwable, Option[Skill]] = ZIO.none

      override def save(skill: Skill): ZIO[Any, Throwable, Unit] = ZIO.unit

      override def remove(id: SkillId): ZIO[Any, Throwable, Unit] = ZIO.unit
    }
  }

  val live = ZLayer.succeed(Service.live)
}
