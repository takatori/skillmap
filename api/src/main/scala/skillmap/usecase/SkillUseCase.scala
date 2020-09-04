package skillmap.usecase

import skillmap.domain.failure.{DBFailure, ExpectedFailure, NotFoundFailure}
import skillmap.domain.skill
import skillmap.domain.skill.{Skill, SkillId, SkillRepository}
import skillmap.infrastructure.id
import skillmap.infrastructure.id.IdFactory
import zio.{Has, ULayer, ZIO, ZLayer}

object SkillUseCase {

  trait Service {
    def get(id: SkillId): ZIO[SkillRepository, ExpectedFailure, Skill]
    def register(name: String, description: Option[String]): ZIO[SkillRepository with IdFactory, ExpectedFailure, Unit]
    def remove(id: SkillId): ZIO[SkillRepository, ExpectedFailure, Unit]
  }

  object Service {
    val live: Service = new Service {
      override def get(id: SkillId): ZIO[SkillRepository, ExpectedFailure, Skill] =
        for {
          skillOpt <- skill.get(id)
          skill    <- ZIO.fromOption(skillOpt).mapError(_ => NotFoundFailure(""))
        } yield skill

      override def register(
          name: String,
          description: Option[String]
      ): ZIO[SkillRepository with IdFactory, ExpectedFailure, Unit] =
        for {
          id <- id.generate()
          s = Skill(SkillId(id), name, description)
          _ <- skill.save(s).mapError(e => DBFailure(e))
        } yield ()

      override def remove(id: SkillId): ZIO[SkillRepository, ExpectedFailure, Unit] =
        for {
          _ <- skill.remove(id)
        } yield ()
    }
  }

  val live: ULayer[Has[Service]] = ZLayer.succeed(Service.live)
}
