package skillmap.usecase.skill

import skillmap.domain.failure.{DBFailure, ExpectedFailure, NotFoundFailure}
import skillmap.domain.skill.{Skill, SkillId, SkillRepository}
import skillmap.infrastructure.id.IdFactory
import zio.{ZIO, ZLayer}

object SkillUseCase {

  trait Service {
    def get(id: SkillId): ZIO[Any, ExpectedFailure, Skill]
    def register(name: String, description: Option[String]): ZIO[Any, ExpectedFailure, Unit]
    def remove(id: SkillId): ZIO[Any, ExpectedFailure, Unit]
  }

  object Service {
    def live(repo: SkillRepository.Service, idFactory: IdFactory.Service): Service = new Service {
      override def get(id: SkillId): ZIO[Any, ExpectedFailure, Skill] =
        for {
          skillOpt <- repo.get(id)
          skill    <- ZIO.fromOption(skillOpt).orElseFail(NotFoundFailure(s"$id not found."))
        } yield skill

      override def register(
          name: String,
          description: Option[String]
      ): ZIO[Any, ExpectedFailure, Unit] =
        for {
          id <- idFactory.generate()
          s = Skill(SkillId(id), name, description)
          _ <- repo.save(s).mapError(e => DBFailure(e))
        } yield ()

      override def remove(id: SkillId): ZIO[Any, ExpectedFailure, Unit] =
        for {
          _ <- repo.remove(id)
        } yield ()
    }
  }

  val live: ZLayer[SkillRepository with IdFactory, Nothing, SkillUseCase] =
    ZLayer.fromServices[SkillRepository.Service, IdFactory.Service, SkillUseCase.Service] { (repo, idFactory) =>
      Service.live(repo, idFactory)
    }
}
