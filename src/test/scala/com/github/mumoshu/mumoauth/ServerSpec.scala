package com.github.mumoshu.mumoauth

import org.specs2.mutable._

object ServerSpec extends Specification {

  val clientIdentifier = "dpf43f3p2l4k3l03"
  val clientSharedSecret = "kd94hf93k423kf44"
  val printerWebSite = "printer.example.com"
  val photosWebSite = "photos.example.com"
  val signatureMethod = "HMAC-SHA1"

  val temporaryCredentialRequestURI = "https://photos.example.com/initiate"
  val resourceOwnerAuthorizationURI = "https://photos.example.com/authorize"
  val tokenRequestURI = "https://photos.example.com"

  trait ResponseGenerator {
    def ok(contentType: String, body: String): Response

    def redirect(url: String, setCookie: Option[String]): Response
  }

  case class FakeResponse(
    status: Int,
    contentType: Option[String] = None,
    body: Option[String] = None,
    location: Option[String] = None,
    httpVersion: String = "1.1") extends Response

  object FakeResponseGenerator extends ResponseGenerator {
    def ok(contentType: String, body: String): Response = FakeResponse(
      status = 200,
      contentType = Some(contentType),
      body = Some(body)
    )

    def redirect(url: String, setCookie: Option[String] = None): Response = FakeResponse(
      status = 303,
      httpVersion = "1.1",
      location = Some(url)
    )
  }

  // The OAuth client must implement this to communicate with the server.
  trait RequestGenerator {

    def get(path: String, queryString: Option[String], host: String): Request

    def post(path: String, host: String, contentType: Option[String], body: Option[String], authorization: Option[String]): Request
  }

  object FakeRequestGenerator extends RequestGenerator {

    def post(path: String, host: String, contentType: Option[String] = None, body: Option[String] = None, authorization: Option[String] = None) = FakeRequest(
      method = "POST",
      path = path,
      host = host,
      contentType = contentType,
      messageBody = body,
      authorization = authorization
    )

    def get(path: String, queryString: Option[String] = None, host: String) = FakeRequest(
      method = "GET",
      path = path,
      queryString = queryString,
      host = host
    )
  }

  trait FakeServer {

    def receive(r: Request): Response = {
      FakeResponseGenerator.ok(
        contentType = "application/x-www-form-urlencoded",
        body = "ok"
      )
    }
  }

  val server = new FakeServer {
    //    val tokenCredentialsStore = FakeTokenCredentialsStore.empty
    //    val temporaryCredentialsStore = FakeTemporaryCredentialsStore.empty
    //    val userSessionsStore = FakeUserSessionsStore()
  }

  "The server" should {

    "validate the request and replies with a set of temporary credentials" in {

      val request = FakeRequestGenerator.post(
        path = "/initiate",
        host = "photos.example.com",
        authorization = Some("""OAuth realm="Photos",oauth_consumer_key="dpf43f3p2l4k3l03",oauth_signature_method="HMAC-SHA1",oauth_timestamp="137131200",oauth_nonce="wIjqoS",oauth_callback="http%3A%2F%2Fprinter.example.com%2Fready",oauth_signature="74KNZJeDHnMBp0EMJ9ZHt%2FXKycU%3D"""")
      )

      val expected = FakeResponseGenerator.ok(
        contentType = "application/x-www-form-urlencoded",
        body = "oauth_token=hh5s93j4hdidpola&oauth_token_secret=hdhd0244k9j7ao03&oauth_callback_confirmed=true"
      )

      val actual = server.receive(request)

      actual must beEqualTo(expected)
    }

    "requests Jane to sign in using her username and password" in {

      val request = FakeRequestGenerator.get(
        host = photosWebSite,
        path = "/login",
        queryString = Some("token??")
      )

      val expected = FakeResponseGenerator.ok(
        contentType = "text/html",
        body = "A html content typically contains a login form"
      )

      val actual = server.receive(request)

      actual must beEqualTo(expected)
    }

    // Not in spec
    "make Jane logged in" in {

      val request = FakeRequestGenerator.post(
        host = photosWebSite,
        path = "/login",
        contentType = Some("application/x-www-form-urlencoded"),
        body = Some("username=jane&password=1234")
      )

      val expected = FakeResponseGenerator.redirect(
        url = "https://photos.example.com/accses_grant",
        setCookie = Some("sid=foo; domain=photos.example.com; path=/; secure")
      )

      val actual = server.receive(request)

      actual must beEqualTo(expected)
    }

    "asks Jane to approve granting 'printer.example.com' access to her private photos" in {

      val request = FakeRequestGenerator.get(
        host = photosWebSite,
        path = "/access_grant"
      )

      val expected = FakeResponseGenerator.ok(
        contentType = "text/html",
        body = "A html content typically contains a set of buttons to authorize or deny access of 'printer.example.com'"
      )

      val actual = server.receive(request)

      actual must beEqualTo(expected)
    }

    "sends redirect to callback URI provided by the client" in {

      val request: Request = FakeRequestGenerator.post(
        host = "photos.example.com",
        path = "/access_grant"
      )

      val expected: Response = FakeResponseGenerator.redirect("http://printer.example.com/ready?oauth_token=hh5s93j4hdidpola&oauth_verifier=hfdp7dh39dks9884")

      val actual: Response = server.receive(request)

      actual must beEqualTo(expected)
    }

    "validates the request and and replies with a set of token credentials in the body of the HTTP response" in {

      val request = FakeRequestGenerator.post(
        host = "photos.example.net",
        path = "/token",
        authorization = Some("""OAuth realm="Photos",oauth_consumer_key="dpf43f3p2l4k3l03",oauth_token="hh5s93j4hdidpola",oauth_signature_method="HMAC-SHA1",oauth_timestamp="137131201",oauth_nonce="walatlh",oauth_verifier="hfdp7dh39dks9884",oauth_signature="gKgrFCywp7rO0OXSjdot%2FIHF7IU%3D"""")
      )

      val expected = FakeResponseGenerator.ok(
        contentType = "application/x-www-form-urlencoded",
        body = "oauth_token=nnch734d00sl2jdk&oauth_token_secret=pfkkdhi9sl3r4s00"
      )

      val actual = server.receive(request)

      actual must beEqualTo(expected)
    }

  }

}
