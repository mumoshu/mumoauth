package models

import oauth2.entity.Client
import oauth2.value_object.GrantType
import oauth2.definition.ClientGrantTypeService

object ClientGrantTypeSvc extends ClientGrantTypeService {

  /**
   * Return grant types supported by the client
   * @param client
   * @return
   */
  def findByClient(client: Client): Seq[GrantType] = {
    Seq(GrantType.Code, GrantType.ClientCredentials, GrantType.Password, GrantType.RefreshToken)
  }

  /**
   * Add a supported grant type to the client
   * @param client
   * @param grantType
   */
  def addGrantType(client: Client, grantType: GrantType) {

  }
}
