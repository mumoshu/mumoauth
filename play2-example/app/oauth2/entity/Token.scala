package oauth2.entity

import org.joda.time.DateTime
import oauth2.value_object.Scope

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
