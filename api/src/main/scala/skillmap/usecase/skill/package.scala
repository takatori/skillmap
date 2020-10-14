package skillmap.usecase

import skillmap.domain.failure.ApplicationError
import skillmap.domain.skill.{Skill, SkillId}
import zio.{Has, ZIO}

package object skill {
  type SkillUseCase = Has[SkillUseCase.Service]

  def get(id: SkillId): ZIO[SkillUseCase, ApplicationError, Skill] =
    ZIO.accessM(_.get.get(id))

  def register(name: String, description: Option[String]): ZIO[SkillUseCase, ApplicationError, Unit] =
    ZIO.accessM(_.get.register(name, description))

  def remove(id: SkillId): ZIO[SkillUseCase, ApplicationError, Unit] =
    ZIO.accessM(_.get.remove(id))
}
