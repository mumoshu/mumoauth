package models

import oauth2.entity.{Client}
import oauth2.service.ClientService

object ClientSvc extends ClientService {
  var clients = Map("test" -> Client("test", "pass"))
  def find(id: String): Option[Client] = {
    clients.get(id)
  }
  def save(client: Client): Client = {
    clients ++= Map(client.id -> client)
    client
  }

}
