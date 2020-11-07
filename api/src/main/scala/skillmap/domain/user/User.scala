package skillmap.domain.user

import eu.timepit.refined.api.Refined
import eu.timepit.refined.boolean.And
import eu.timepit.refined.char.LetterOrDigit
import eu.timepit.refined.collection.{Forall, MaxSize, NonEmpty}
import eu.timepit.refined.refineV
import eu.timepit.refined.string.Uuid
import io.estatico.newtype.macros.newtype
import skillmap.domain.user.User.{UserId, UserName}

final case class User(id: UserId, name: UserName)

object User {

  type UserIdRule   = Uuid
  type UserIdString = String Refined UserIdRule
  @newtype case class UserId(value: UserIdString)
  object UserId {
    //implicit val eq: Eq[UserId] = implicitly[Eq[String]].coerce
    def apply(rawUserId: String): Either[String, UserId] =
      refineV[UserIdRule](rawUserId).map(UserId(_))
  }

  type UserNameRule   = NonEmpty And MaxSize[256] And Forall[LetterOrDigit]
  type UserNameString = String Refined UserNameRule
  @newtype case class UserName(value: UserNameString)
  object UserName {
    def apply(rawUserName: String): Either[String, UserName] =
      refineV[UserNameRule](rawUserName).map(UserName(_))
  }
}
