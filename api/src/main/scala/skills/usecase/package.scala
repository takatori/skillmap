package skills

import zio.Has

package object usecase {
  type UserUseCase = Has[UserUseCase.Service]
}
