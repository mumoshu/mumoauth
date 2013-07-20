package oauth2.service

import models.ClientGrantTypeSvc
import oauth2.definition.ClientGrantTypeService
import oauth2.entity.Client
import oauth2.value_object.GrantType

/**
  * The client service stub
  */
trait ClientService {
   implicit def toMapped(client: Client) = new {
     def authorizedGrantTypes(implicit service: ClientGrantTypeService): Seq[GrantType] = {
       service.findByClient(client)
     }
     def addGrantType(grantType: GrantType)(implicit service: ClientGrantTypeService) {
       service.addGrantType(client, grantType)
     }
   }
   def find(id: String): Option[Client]
   def save(client: Client): Client
 }
