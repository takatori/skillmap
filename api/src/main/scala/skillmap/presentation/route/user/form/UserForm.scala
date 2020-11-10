package skillmap.presentation.route.user.form

import skillmap.domain.user.User.UserName
import zio.{IO, ZIO}

final case class UserForm(name: String) {
  def validate: IO[String, UserName] = ZIO.fromEither(UserName(name))
}
