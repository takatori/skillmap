package skillmap.presentation.route.user.response

import skillmap.domain.user.User

final case class UserResponse(id: String, name: String)

object UserResponse {
  def from(user: User): UserResponse = UserResponse(user.id.value, user.name)
}
