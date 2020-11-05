package skillmap.domain.user

import eu.timepit.refined.api.Refined
import eu.timepit.refined.boolean.And
import eu.timepit.refined.char.{LetterOrDigit, LowerCase}
import eu.timepit.refined.refineV
import io.estatico.newtype.macros.newtype
import skillmap.domain.user.UserId.UserIdString

@newtype case class UserId(value: UserIdString)
object UserId {
  type UserIdRule   = LetterOrDigit And LowerCase
  type UserIdString = String Refined UserIdRule

  def apply(rawUserId: String): Either[String, UserId] =
    refineV[UserIdRule](rawUserId).map(UserId(_))
}
