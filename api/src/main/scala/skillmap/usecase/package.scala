package skillmap

import skillmap.usecase.skill.SkillUseCase
import skillmap.usecase.user.UserUseCase
import zio.Has

package object usecase {
  type UserUseCase  = Has[UserUseCase.Service]
  type SkillUseCase = Has[SkillUseCase.Service]
}
