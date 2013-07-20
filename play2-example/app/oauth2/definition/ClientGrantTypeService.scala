package oauth2.definition

import oauth2.entity.Client
import oauth2.value_object.GrantType

trait ClientGrantTypeService {
  def addGrantType(client: Client, grantType: GrantType): Unit
  def findByClient(client: Client): Seq[GrantType]
}
