package oauth2

import java.net.{URLDecoder, URLEncoder, URI}

object Utils {
  def isMalformed(uri: String) = try {
    new URI(uri)
    false
  } catch {
    case _ =>
    true
  }
  def uriEncode(str: String) = URLEncoder.encode(str, "utf-8")
  def uriDecode(str: String) = URLDecoder.decode(str, "utf-8")
}
