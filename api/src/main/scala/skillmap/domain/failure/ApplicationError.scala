package skillmap.domain.failure

sealed abstract class ApplicationError            extends Exception
case class NotFoundFailure(message: String)       extends ApplicationError
case class AuthenticationFailure(message: String) extends ApplicationError
