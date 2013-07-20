package oauth2

case class AuthorizationRequest(
  responseType: String,
  clientId: Option[String],
  redirectURI: Option[String],
  scope: Option[String],
  status: Option[String]
)
