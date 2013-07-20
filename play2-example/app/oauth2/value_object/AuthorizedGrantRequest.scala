package oauth2.value_object

case class AuthorizedGrantRequest(responseType: String, clientId: String, redirectionURI: String, requestedScope: String, authorizedScope: String, state: Option[String])
