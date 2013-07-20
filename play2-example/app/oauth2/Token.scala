package oauth2

import org.joda.time.DateTime

/**
 *
 * @param accessToken
 * @param accessTokenType "Bearer"
 * @param refreshToken
 * @param expirationDate
 * @param scope
 */
case class Token(accessToken: String, accessTokenType: String, refreshToken: Option[String], expirationDate: DateTime, scope: Scope) {
  def expiresIn = (expirationDate.getMillis - new DateTime().getMillis) / 1000
  def isExpired = expiresIn <= 0
}
