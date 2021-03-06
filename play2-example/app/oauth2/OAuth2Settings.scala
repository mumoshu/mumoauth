package oauth2

case class OAuth2Settings(
  clientId: String,
  clientSecret: String,
  authorizationEndpoint: String,
  tokenEndpoint: String,

  /**
   * If true, include the client credentials in the request body to authenticate clients.
   * If not, use HTTP Basic Authentication scheme to authenticate clients.
   * @see http://tools.ietf.org/html/draft-ietf-oauth-v2-31#section-2.3.1
   */
  requiresClientSecretInRequestParameter: Boolean = false,

  /**
   * "The authorization server SHOULD document the size of any identifier it issues."
   * @see http://tools.ietf.org/html/draft-ietf-oauth-v2-31#section-2.2
   */
  sizeOfIdentifier: Int = 1024
) {
  def useBasicAuthForClientAuthentication = !requiresClientSecretInRequestParameter
}
