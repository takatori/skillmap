package skillmap.domain.failure

sealed abstract class ExpectedFailure             extends Exception
case class DBFailure(throwable: Throwable)        extends ExpectedFailure
case class NotFoundFailure(message: String)       extends ExpectedFailure
case class AuthenticationFailure(message: String) extends ExpectedFailure
