package skillmap.usecase.skill

import skillmap.domain.failure.{ApplicationError, NotFoundFailure}
import skillmap.domain.skill.{Skill, SkillId, SkillRepository}
import skillmap.infrastructure.id.IdFactory
import zio.{ZIO, ZLayer}

object SkillUseCase {

  trait Service {
    def get(id: SkillId): ZIO[Any, NotFoundFailure, Skill]
    def register(name: String, description: Option[String]): ZIO[Any, Nothing, Unit]
    def remove(id: SkillId): ZIO[Any, Nothing, Unit]
  }

  object Service {
    def live(repo: SkillRepository.Service, idFactory: IdFactory.Service): Service = new Service {
      override def get(id: SkillId): ZIO[Any, NotFoundFailure, Skill] =
        for {
          skillOpt <- repo.get(id).orDie
          skill    <- ZIO.fromOption(skillOpt).orElseFail(NotFoundFailure(s"$id not found."))
        } yield skill

      override def register(
          name: String,
          description: Option[String]
      ): ZIO[Any, Nothing, Unit] =
        for {
          id <- idFactory.generate()
          s = Skill(SkillId(id), name, description)
          _ <- repo.save(s).orDie
        } yield ()

      override def remove(id: SkillId): ZIO[Any, Nothing, Unit] =
        for {
          _ <- repo.remove(id).orDie
        } yield ()
    }
  }

  val live: ZLayer[SkillRepository with IdFactory, Nothing, SkillUseCase] =
    ZLayer.fromServices[SkillRepository.Service, IdFactory.Service, SkillUseCase.Service] { (repo, idFactory) =>
      Service.live(repo, idFactory)
    }
}
