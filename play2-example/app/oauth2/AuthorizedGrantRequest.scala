package oauth2

case class AuthorizedGrantRequest(responseType: String, clientId: String, redirectionURI: String, requestedScope: String, authorizedScope: String, state: Option[String])
