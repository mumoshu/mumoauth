package oauth2

trait GrantTypeService {
  def addGrantType(client: Client, grantType: GrantType): Unit
  def findGrantTypes(client: Client): Seq[GrantType]
}
