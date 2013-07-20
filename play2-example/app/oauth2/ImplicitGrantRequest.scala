package oauth2

case class ImplicitGrantRequest(client: Client, redirectionURI: String, requestedScope: Scope, state: Option[String]) extends GrantRequest {
  val responseType = ResponseType.Token
}

