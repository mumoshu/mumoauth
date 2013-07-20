package oauth2

import org.apache.commons.codec.binary.Base64

object AuthorizationHeader {
  val Name = "Authorization"
  def valueFor(oauth2Settings: OAuth2Settings): String =
    "Basic " + Base64.encodeBase64String((oauth2Settings.clientId + ":" + oauth2Settings.clientSecret).getBytes("utf-8"))
}
