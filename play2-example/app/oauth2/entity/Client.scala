package oauth2.entity

import oauth2.value_object.ClientType

/**
 * @see http://tools.ietf.org/html/draft-ietf-oauth-v2-31#section-2.3.1
 * @param id client_id REQUIRED.  The client identifier issued to the client during
         the registration process described by Section 2.2.
 * @param password client_secret REQUIRED.  The client secret.  The client MAY omit the
         parameter if the client secret is an empty string.
 * @param clientType
 * @param redirectionURI
 */
case class Client(id: String, password: String, redirectionURI: Option[String] = None, clientType: ClientType = ClientType.Confidential) {
  val implicitGrantEnabled: Boolean = true

  /**
   * The authorization server MUST require the following clients to
   * register their redirection endpoint:
   * - Public clients.
   o -  Confidential clients utilizing the implicit grant type.
   * @see http://tools.ietf.org/html/draft-ietf-oauth-v2-31#section-3.1.2.2
   * @return
   */
  def requiresRedirectionURI: Boolean =
    clientType == ClientType.Public || clientType == ClientType.Confidential && implicitGrantEnabled
}
