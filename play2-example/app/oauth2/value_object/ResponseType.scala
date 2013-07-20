package oauth2.value_object

sealed trait ResponseType {
  def asString: String
}

object ResponseType {

  private case class ResponseTypeImpl(asString: String) extends ResponseType

  private val CodeAsString = "code"
  private val TokenAsString = "token"

  def apply(str: String): ResponseType = str match {
    case CodeAsString => Code
    case TokenAsString => Token
  }

  def parseString(str: String): Either[Exception, ResponseType] = str match {
    case CodeAsString => Right(Code)
    case TokenAsString => Right(Token)
    case invalidStr => Left(new Exception("\"" + invalidStr + "\" is not a valid response type. \"" + CodeAsString + "\" or \"" + TokenAsString + "\" is valid."))
  }

  case object Code extends ResponseType {
    val asString = CodeAsString
  }

  case object Token extends ResponseType {
    val asString = TokenAsString
  }
}
