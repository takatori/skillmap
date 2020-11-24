package skillmap.presentation.route.skill.form

import skillmap.domain.skill.Skill.SkillId
import sttp.tapir.CodecFormat.TextPlain
import sttp.tapir.{Codec, DecodeResult}

object SkillIdCodec {

  def decode(id: String): DecodeResult[SkillId] = SkillId(id) match {
    case Right(u) => DecodeResult.Value(u)
    case Left(s)  => DecodeResult.Error(id, new Throwable(s))
  }
  def encode(id: SkillId): String = id.value.value

  implicit val skillIdCodec: Codec[String, SkillId, TextPlain] =
    Codec.string.mapDecode(decode)(encode)
}
