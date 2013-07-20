package oauth2.service

trait RefreshTokenService {
  def find(ref: String): Option[String]
  def delete(ref: String): Unit
}
