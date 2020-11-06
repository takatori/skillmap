package skillmap.domain.user

import eu.timepit.refined.api.Refined
import eu.timepit.refined.refineV
import eu.timepit.refined.string.Uuid
import io.estatico.newtype.macros.newtype
import skillmap.domain.user.User.UserId

final case class User(id: UserId, name: String)

object User {

  type UserIdRule   = Uuid
  type UserIdString = String Refined UserIdRule
  @newtype case class UserId(value: UserIdString)
  object UserId {
    def apply(rawUserId: String): Either[String, UserId] =
      refineV[UserIdRule](rawUserId).map(UserId(_))
  }

}
