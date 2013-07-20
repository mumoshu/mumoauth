package oauth2

object AuthorizationCodeGrant {
  // @see http://tools.ietf.org/html/draft-ietf-oauth-v2-31#section-4.1.3
  object AccessTokenRequestParameterNames {
    val GrantType = "grant_type"
    val Code = "code"
    val RedirectURI = "redirect_uri"
    val ClientId = "client_id"
  }

  /**
   * @see http://tools.ietf.org/html/draft-ietf-oauth-v2-31#section-4.1.4
   * @see http://tools.ietf.org/html/draft-ietf-oauth-v2-31#section-5.1
   */
  object AccessTokenResponseParameterNames {
    /**
     * REQUIRED.  The access token issued by the authorization server.
     */
    val AccessToken = "access_token"
    /**
     * REQUIRED.  The type of the token issued
     */
    val TokenType = "token_type"
    /**
     * RECOMMENDED.  The lifetime in seconds of the access token.  For
     * example, the value "3600" denotes that the access token will
     * expire in one hour from the time the response was generated.
     * If omitted, the authorization server SHOULD provide the
     * expiration time via other means or document the default value.
     */
    val ExpiresIn = "expires_in"
    /**
     * OPTIONAL.  The refresh token, which can be used to obtain new
     * access tokens using the same authorization grant
     */
    val RefreshToken = "refresh_token"
  }
}
