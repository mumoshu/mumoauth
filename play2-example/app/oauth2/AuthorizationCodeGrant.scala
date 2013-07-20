package oauth2

import models.GrantType

object AuthorizationCodeGrant {
  object AcecssTokenRequest {
    def getParametersAndHeaders(
      code: String,
      redirectURI: String,
      oauth2Settings: OAuth2Settings
   ): (Map[String, Seq[String]], Map[String, String]) = {
      /**
       * 4.1.3 Access Token Request
       * http://tools.ietf.org/html/draft-ietf-oauth-v2-31#section-4.1.3
       */
      val params = Map(
        AccessTokenRequestParameterNames.GrantType -> Seq(GrantType.Code.asString),
        AccessTokenRequestParameterNames.Code -> Seq(code),
        AccessTokenRequestParameterNames.RedirectURI -> Seq(redirectURI),
        AccessTokenRequestParameterNames.ClientId -> Seq(oauth2Settings.clientId)
      ) ++ {
        if (oauth2Settings.requiresClientSecretInRequestParameter)
          Map(
            "client_secret" -> Seq(oauth2Settings.clientSecret)
          )
        else
          Map.empty
      }
      val authHeaderValue = AuthorizationHeader.valueFor(oauth2Settings)
      (
        params,
        Map(AuthorizationHeader.Name -> AuthorizationHeader.valueFor(oauth2Settings))
      )
    }
  }

  /**
   * @see http://tools.ietf.org/html/draft-ietf-oauth-v2-31#section-4.1.3
   */
  object AccessTokenRequestParameterNames {
    /**
     * REQUIRED.  Value MUST be set to "authorization_code"
     */
    val GrantType = "grant_type"
    /**
     * REQUIRED.  The authorization code received from the
     * authorization server.
     */
    val Code = "code"
    /**
     * REQUIRED, if the "redirect_uri" parameter was included in the
     * authorization request as described in Section 4.1.1, and their
     * values MUST be identical.
     */
    val RedirectURI = "redirect_uri"
    /**
     * REQUIRED, if the client is not authenticating with the
     * authorization server as described in Section 3.2.1.
     */
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
