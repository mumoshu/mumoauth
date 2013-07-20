package oauth2

sealed trait GrantType {
  def grantType: String
  def asString = grantType
}

object GrantType {

  private val AuthorizationCodeAsString = "authorization_code"
  private val TokenAsString = "token"

  def apply(str: String): GrantType = str match {
    case AuthorizationCodeAsString => Code
    case TokenAsString => Token
  }

  case object Code extends GrantType {
    val grantType = AuthorizationCodeAsString
  }

  case object Token extends GrantType {
    val grantType = TokenAsString
  }
}
