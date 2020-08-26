package skillmap.presentation.response

sealed trait ErrorResponse                              extends Product with Serializable
case class InternalServerErrorResponse(message: String) extends ErrorResponse
case class NotFoundResponse(message: String)            extends ErrorResponse
case class BadRequestResponse(message: String)          extends ErrorResponse
