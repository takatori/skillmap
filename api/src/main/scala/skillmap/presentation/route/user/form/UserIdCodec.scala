package skillmap.presentation.route.user.form

import skillmap.domain.user.User.UserId
import sttp.tapir.CodecFormat.TextPlain
import sttp.tapir.{Codec, DecodeResult}

object UserIdCodec {

  def decode(id: String): DecodeResult[UserId] = UserId(id) match {
    case Right(u) => DecodeResult.Value(u)
    case Left(s)  => DecodeResult.Error(id, new Throwable(s))
  }
  def encode(id: UserId): String = id.value.value

  implicit val userIdCodec: Codec[String, UserId, TextPlain] =
    Codec.string.mapDecode(decode)(encode)

}
