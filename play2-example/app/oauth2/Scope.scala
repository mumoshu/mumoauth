package oauth2

/**
 * Scope of authorizations to take control of accesses to protected resources
 *
 * @see http://tools.ietf.org/html/draft-ietf-oauth-v2-31#section-3.3
 */
trait Scope {
   def value: Int
   def scope: String
}
