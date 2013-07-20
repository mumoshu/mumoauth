package oauth2

package object server {
  sealed trait AuthzServerError
  object ClientNotFoundError extends AuthzServerError
  case class InvalidRequestError(description: String) extends AuthzServerError
}
