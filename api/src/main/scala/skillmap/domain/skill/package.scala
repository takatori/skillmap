package skillmap.domain

import zio.{Has, ZIO}

package object skill {
  type SkillRepository = Has[SkillRepository.Service]

  def get(id: SkillId): ZIO[SkillRepository, Throwable, Option[Skill]] =
    ZIO.accessM(_.get.get(id))

  def save(skill: Skill): ZIO[SkillRepository, Throwable, Unit] =
    ZIO.accessM(_.get.save(skill))

  def remove(id: SkillId): ZIO[SkillRepository, Throwable, Unit] =
    ZIO.accessM(_.get.remove(id))

}
