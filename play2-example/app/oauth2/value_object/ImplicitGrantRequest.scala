package oauth2.value_object

import oauth2.entity.Client
import oauth2.value_object.GrantRequest

case class ImplicitGrantRequest(client: Client, redirectionURI: String, requestedScope: Scope, state: Option[String]) extends GrantRequest {
  val responseType = ResponseType.Token
}

