package models

import oauth2.entity.{Client}
import oauth2.service.ClientService

object ClientSvc extends ClientService {
  var clients = Map("test" -> Client("test", "pass"))
  def find(id: String): Option[Client] = {
    clients.get(id)
  }
  def save(client: Client): Client = {
    if (client.requiresRedirectionURI && client.redirectionURI.isEmpty)
      throw new RuntimeException("Redirection URI must be provided in order to register this client. See http://tools.ietf.org/html/draft-ietf-oauth-v2-31#section-3.1.2.2 for details.")
    clients ++= Map(client.id -> client)
    client
  }

}
