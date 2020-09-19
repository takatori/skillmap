package skillmap.usecase

import zio.Has

package object skill {
  type SkillUseCase = Has[SkillUseCase.Service]

}
