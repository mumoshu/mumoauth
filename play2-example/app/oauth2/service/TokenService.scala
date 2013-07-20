package oauth2.service

import oauth2.error.TokenError
import oauth2.entity.{Code, Token}
import oauth2.{Utils}
import oauth2.value_object.Scope

trait TokenService {
  trait BaseParamBuilder {
    val token: Token

    /**
     * Build a successful access token response
     * See: http://tools.ietf.org/html/draft-ietf-oauth-v2-31#section-5.1
     * @param requestedScope
     * @return
     */
    def buildParamsMap(requestedScope: Scope): Map[String, Any] = {
      Map(
        "access_token" -> token.accessToken,
        "token_type" -> token.accessTokenType,
        "expires_in" -> token.expiresIn
      ) ++ token.refreshToken.map(t => Map("refresh_token" -> t)).getOrElse(Map.empty) ++
        (if (token.scope != requestedScope) Map("scope" -> token.scope.scope) else Map.empty)
    }
  }

  implicit def toURIComponentBuilder(_token: Token) = new BaseParamBuilder {
    val token = _token

    def buildURIComponent(requestedScope: Scope, state: Option[String]): String = {
      val params = buildParamsMap(requestedScope) ++ state.map(s => Map("state" -> s)).getOrElse(Map.empty)
      params.map { case (key, value) => Utils.uriEncode(key) + "=" + Utils.uriEncode(value.toString) }.mkString("&")
    }
  }

  def find(token: String): Option[Token]

  def save(token: Token): Token

  def issue(code: Code): (Scope, Token)
  def issue(authorizedScope: Scope): Token
  def issue(grantType: String, code: String, clientId: String, redirectURI: Option[String]): Either[TokenError, (Scope, Token)]
}
