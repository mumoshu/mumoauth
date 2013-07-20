package oauth2.value_object

import oauth2.entity.Client

trait GrantRequest {
  val client: Client
  val redirectionURI: String
  val requestedScope: Scope
  val state: Option[String]
  val responseType: ResponseType
}
