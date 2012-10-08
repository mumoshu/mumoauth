import org.specs2.execute.Result
import org.specs2.mutable._
import play.api.http.Status
import play.api.libs.ws.WS
import play.api.test._
import play.api.test.Helpers._

object OAuth2ImplSpec extends Specification {

  sequential

  object server extends Around {
    def around[T <% Result](t: =>T) = running(TestServer(9000, FakeApplication())) {
      t
    }
  }

  // valid authorization request
  "The server must accepts an valid authorization request" in server {
    val response = WS.url("http://localhost:9000/authorize").withQueryString("response_type" -> "code").get().await.get

    response.status must beEqualTo (Status.OK)
  }

  // invalid authorization request
  "The server must deny an invalid authorization request" in server {
    val response = WS.url("http://localhost:9000/authorize").withQueryString("response_type" -> "hoge").get().await.get

    response.status must beEqualTo (Status.BAD_REQUEST)
  }

  // valid token request
  "The server must provide an valid token" in server {
    val body = Map(
      "grant_type" -> Seq("authorization_code"),
      "code" -> Seq("myAuthorizationCode"),
      "redirect_uri" -> Seq("http://localhost:9000/callback"),
      "client_id" -> Seq("myClientId")
    )
    val response = WS.url("http://localhost:9000/token").post(body).await.get

    response.status must beEqualTo (Status.OK)
  }

  // Actually, the authorization server MUST require the use of TLS when sending passwords
  "The server must provide an valid token for a refresh token" in server {
    val body = Map(
      "grant_type" -> Seq("refresh_token"),
      "refresh_token" -> Seq("myRefreshToken"),
      "client_id" -> Seq("myClientId"),
      "client_secret" -> Seq("myClientSecret")
    )
    val response = WS.url("http://localhost:9000/token").post(body).await.get

    response.status must beEqualTo (Status.OK)
  }

  // invalid token request
  "The server must send an error" in server {
    val response = WS.url("http://localhost:9000/token").get().await.get

    response.status must not equalTo (Status.OK)
  }

}
