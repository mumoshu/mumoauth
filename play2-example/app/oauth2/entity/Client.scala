package oauth2.entity

case class Client(id: String, password: String, redirectionURI: Option[String] = None)


