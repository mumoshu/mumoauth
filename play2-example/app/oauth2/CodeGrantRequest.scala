package oauth2

case class CodeGrantRequest(client: Client, redirectionURI: String, requestedScope: Scope, state: Option[String]) extends GrantRequest {
  val responseType = ResponseType.Code
}
