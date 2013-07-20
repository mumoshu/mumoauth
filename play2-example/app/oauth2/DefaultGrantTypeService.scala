package oauth2

object DefaultGrantTypeService extends GrantTypeService {
  def addGrantType(client: Client, grantType: GrantType) = {

  }
  def findGrantTypes(client: Client): Seq[GrantType] = {
    Seq(GrantType.Code, GrantType.Token)
  }
}
