package oauth2.value_object

case class AuthorizationRequest(
  responseType: String,
  clientId: Option[String],
  redirectURI: Option[String],
  scope: Option[String],
  status: Option[String]
)
