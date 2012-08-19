package com.github.mumoshu.mumoauth.oauth2

import org.specs2.mutable._
import com.github.mumoshu.mumoauth.Helpers._
import com.github.mumoshu.mumoauth.test.DependsServer

object AuthorizationCodeGrantTypeSpec extends Specification {

  //
  // (A) Client |-- Authorization Request -->| Resource Owner
  //
  // The client requests authorization from the resource owner.  The
  // authorization request can be made directly to the resource owner
  // (as shown), or preferably indirectly via the authorization
  // server as an intermediary.

  //
  // (B) Client |<-- Authorization Grant --| Resource Owner
  //
  // The client receives an authorization grant which is a credential
  //  representing the resource owner's authorization, expressed using
  //    one of four grant types defined in this specification or using
  //  an extension grant type.  The authorization grant type depends
  //  on the method used by the client to request authorization and
  //  the types supported by the authorization server.

  // (C) Authorization Grant is sent to Authorization Server

  // (D) Access Token is sent to Client

  // (E) Access Token is sent to Resource Server

  // (F) Client has access to Protected Resource

  val ClientIdentifier = "s6BhdRkqt3"
  val ClientSecret = "7Fjfp0ZBr1KtDRbnfVdmIw"
  val BasicAuthentication = "Basic czZCaGRSa3F0MzpnWDFmQmF0M2JW"
  val AuthorizationEndpoint = "http://example.com/token"
  val RedirectionEndpoint = "https://client.example.com/cb"

  case class Client(
    /*
     * Basic czZCaGRSa3F0MzpnWDFmQmF0M2JW
     */
    identifier: String,
    /*
     * The client secret.  The client MAY omit the
     * parameter if the client secret is an empty string.
     */
    secret: String = "")

  val client = Client(ClientIdentifier, ClientSecret)

  "Authorization server" should {

    // client から server へのリクエスト。client はリフレッシュトークンと引換に、アクセストークンを得る。
    "issue Client with an Access Token for a Refresh Token" in new DependsServer {

      override val clientIdentifier = ClientIdentifier
      override val clientSharedSecret = ClientSecret
      override val tokenRequestURI = AuthorizationEndpoint

      val request = FakeRequestGenerator.post(
        scheme = "https",
        path = "/token",
        host = "example.com",
        contentType = Some("application/x-www-form-urlencoded;charset=UTF-8"),
        body = Some("grant_type=refresh_token&refresh_token=tGzv3JOkF0XG5Qx2TlKWIA&client_id=s6BhdRkqt3&client_secret=7Fjfp0ZBr1KtDRbnfVdmIw"),
        authorization = None
      )

      val expected = FakeResponseGenerator.ok(
        contentType = "application/x-www-form-urlencoded",
        body = "oauth_token=hh5s93j4hdidpola&oauth_token_secret=hdhd0244k9j7ao03&oauth_callback_confirmed=true"
      )

      server.receive(request) should beEqualTo(expected)
    }

    "asks Resource Owner to authrize Client's access to Protected Resource" in new DependsServer {

      override val clientIdentifier = ClientIdentifier
      override val clientSharedSecret = ClientSecret
      override val tokenRequestURI = AuthorizationEndpoint

      val request = FakeRequestGenerator.get(
        scheme = "http",
        host = "server.example.com",
        path = "/authorize",
        queryString = Some("response_type=code&client_id=s6BhdRkqt3&state=xyz&redirect_uri=https%3A%2F%2Fclient%2Eexample%2Ecom%2Fcb")
      )

      //      val result = server.acceptAuthorizationRequest(request)

    }

    "sends User Agent back to Client with code" in new DependsServer {
      val code = "SplxlOBeZQQYbYS6WxSbIA"
      val response = server.redirectWithCode(RedirectionEndpoint, code)

      response.location must beSome("https://client.example.com/cb?code=SplxlOBeZQQYbYS6WxSbIA&state=xyz")
      response.status must beEqualTo(302)
    }
  }

}
