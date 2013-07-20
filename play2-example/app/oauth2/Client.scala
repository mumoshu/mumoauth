package oauth2

case class Client(id: String, password: String, redirectionURI: Option[String] = None)

/**
 * The client service stub
 */
trait ClientService {
  implicit def toMapped(client: Client) = new {
    def authorizedGrantTypes(implicit service: GrantTypeService = DefaultGrantTypeService): Seq[GrantType] = {
      service.findGrantTypes(client)
    }
    def addGrantType(grantType: GrantType)(implicit service: GrantTypeService = DefaultGrantTypeService) {
      service.addGrantType(client, grantType)
    }
  }
  def find(id: String): Option[Client]
  def save(client: Client): Client
}
