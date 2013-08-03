package oauth2.value_object

sealed trait GrantType {
  def grantType: String
  def asString = grantType
}

object GrantType {

  /**
   * "4.1.  Authorization Code Grant"
   * @see http://tools.ietf.org/html/draft-ietf-oauth-v2-31#section-4.1.3
   */
  private val AuthorizationCodeAsString = "authorization_code"

  /**
   * "4.3.  Resource Owner Password Credentials Grant"
   * @see http://tools.ietf.org/html/draft-ietf-oauth-v2-31#section-4.3.2
   */
  private val PasswordAsString = "password"
  /**
   * "4.4.  Client Credentials Grant"
   * @see http://tools.ietf.org/html/draft-ietf-oauth-v2-31#section-4.4.2
   */
  private val ClientCredentialsAsString = "client_credentials"
  /**
   * "6.  Refreshing an Access Token"
   * @see http://tools.ietf.org/html/draft-ietf-oauth-v2-31#section-6
   */
  private val RefreshTokenAsString = "refresh_token"

  private val values = Map(
    AuthorizationCodeAsString -> Code,
    PasswordAsString -> Password,
    ClientCredentialsAsString -> ClientCredentials,
    RefreshTokenAsString -> RefreshToken
  )

  def apply(str: String): GrantType = parseString(str).getOrElse {
    throw new RuntimeException(
      "Invalid grant_type '" + str + "' which must be one of " + values.keys.map("'" + _ + "'").mkString(", ") + "."
    )
  }

  def parseString(str: String): Option[GrantType] = values.get(str)

  case object Code extends GrantType {
    val grantType = AuthorizationCodeAsString
  }

  case object Password extends GrantType {
    val grantType = PasswordAsString
  }

  case object ClientCredentials extends GrantType {
    val grantType = ClientCredentialsAsString
  }

  case object RefreshToken extends GrantType {
    val grantType = RefreshTokenAsString
  }

}
