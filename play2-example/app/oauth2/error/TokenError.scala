package oauth2.error

import oauth2.{Utils, TokenErrorResponseBuilder}

sealed trait TokenError {
  val error: String
  val errorDescription: Option[String] = None
  val redirectURI: Option[String] = None
  val errorURI: Option[String] = None
  val state: Option[String] = None

  def buildParamsMap: Map[String, Any] = {
    Map(
      "error" -> error
    ) ++ errorDescription.map(d => Map("error_description" -> d)).getOrElse(Map.empty) ++
    errorURI.map(u => Map("error_uri" -> u)).getOrElse(Map.empty) ++
    state.map(s => Map("state" -> s)).getOrElse(Map.empty)
  }

  def buildResponse[A](implicit builder: TokenErrorResponseBuilder[A]): A = {
    builder.buildResponse(buildParamsMap)
  }

  def buildRedirectionURI: Option[String] = {
    redirectURI.map(u => u + "#" + buildParamsMap.map { case (key, value) => Utils.uriEncode(key) + "=" + Utils.uriEncode(value.toString) }.mkString("&"))
  }
}

object InvalidGrantError extends TokenError {
  val error = "invalid_grant"
}

case class InvalidRequestError(description: String, override val redirectURI: Option[String] = None) extends TokenError {
  val error = "invalid_request"
  override val errorDescription = Some(description)
}

object InvalidClientError extends TokenError {
  val error = "invalid_client"
}

case class InvalidScopeError(description: String, override val redirectURI: Option[String] = None) extends TokenError {
  val error = "invalid_scope"
  override val errorDescription = Some(description)
}

object RedirectURIError extends TokenError {
  val error = "hoge"
}

object UnauthorizedClientError extends TokenError {
  val error = "unauthorized_client"
}

object UnsupportedGrantTypeError extends TokenError {
  val error = "unsupported_grant_type"
}
