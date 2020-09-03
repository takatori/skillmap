package skillmap

import zio.Has

package object usecase {
  type UserUseCase  = Has[UserUseCase.Service]
  type SkillUseCase = Has[SkillUseCase.Service]
}
