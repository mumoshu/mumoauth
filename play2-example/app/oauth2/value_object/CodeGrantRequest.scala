package oauth2.value_object

import oauth2.entity.Client

case class CodeGrantRequest(client: Client, redirectionURI: String, requestedScope: Scope, state: Option[String]) extends GrantRequest {
  val responseType = ResponseType.Code
}
