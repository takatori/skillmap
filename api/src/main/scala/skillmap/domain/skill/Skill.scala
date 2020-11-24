package skillmap.domain.skill

import eu.timepit.refined.api.Refined
import eu.timepit.refined.boolean.And
import eu.timepit.refined.char.LetterOrDigit
import eu.timepit.refined.collection.{Forall, MaxSize, NonEmpty}
import eu.timepit.refined.refineV
import io.estatico.newtype.macros.newtype
import skillmap.domain.skill.Skill.SkillId

final case class Skill(id: SkillId, name: String, description: Option[String])

object Skill {

  type SkillIdRule   = NonEmpty And MaxSize[36] And Forall[LetterOrDigit]
  type SkillIdString = String Refined SkillIdRule
  @newtype case class SkillId(value: SkillIdString)
  object SkillId {
    def apply(rawSkillId: String): Either[String, SkillId] =
      refineV[SkillIdRule](rawSkillId).map(SkillId(_))
  }

}
