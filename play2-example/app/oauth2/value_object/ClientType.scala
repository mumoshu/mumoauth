package oauth2.value_object

/**
 * OAuth defines two client types, based on their ability to
 * authenticate securely with the authorization server (i.e. ability to
 * maintain the confidentiality of their client credentials)
 * @see http://tools.ietf.org/html/draft-ietf-oauth-v2-31#section-2.1
 */
sealed trait ClientType {
  def asString: String
}

object ClientType {

  /**
   * Clients capable of maintaining the confidentiality of their
   * credentials (e.g. client implemented on a secure server with
   * restricted access to the client credentials), or capable of secure
   * client authentication using other means.
   */
  case object Confidential extends ClientType {
    val asString = "confidential"
  }

  /**
   * Clients incapable of maintaining the confidentiality of their
   * credentials (e.g. clients executing on the device used by the
   * resource owner such as an installed native application or a web
   * browser-based application), and incapable of secure client
   * authentication via any other means.
   */
  case object Public extends ClientType {
    val asString = "public"
  }
}
