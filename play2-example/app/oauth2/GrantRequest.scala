package oauth2

trait GrantRequest {
  val client: Client
  val redirectionURI: String
  val requestedScope: Scope
  val state: Option[String]
  val responseType: ResponseType
}
