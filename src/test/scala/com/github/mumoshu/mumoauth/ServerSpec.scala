package com.github.mumoshu.mumoauth

import org.specs2.mutable._
import server._

import Helpers._
import test.DependsServer

object ServerSpec extends Specification with DependsServer {

  "The server" should {

    "validate the request and replies with a set of temporary credentials" in {

      val request = FakeRequestGenerator.post(
        scheme = "https",
        path = "/initiate",
        host = "photos.example.net",
        authorization = Some("""OAuth realm="Photos",oauth_consumer_key="dpf43f3p2l4k3l03",oauth_signature_method="HMAC-SHA1",oauth_timestamp="137131200",oauth_nonce="wIjqoS",oauth_callback="http%3A%2F%2Fprinter.example.com%2Fready",oauth_signature="74KNZJeDHnMBp0EMJ9ZHt%2FXKycU%3D"""")
      )

      val expected = FakeResponseGenerator.ok(
        contentType = "application/x-www-form-urlencoded",
        body = "oauth_token=hh5s93j4hdidpola&oauth_token_secret=hdhd0244k9j7ao03&oauth_callback_confirmed=true"
      )

      val actual = server.receive(request)

      actual must beEqualTo(expected)
    }

    // The client redirects Jane's user-agent to the server's Resource Owner
    // Authorization endpoint to obtain Jane's approval for accessing her
    // private photos.

    // And then, ...

    // (Not defined in the OAuth spec.)
    "redirects Jane to the login page" in {

      val request = FakeRequestGenerator.get(
        scheme = "https",
        host = photosWebSite,
        path = "/authorize",
        queryString = Some("?oauth_token=hh5s93j4hdidpola")
      )

      val expected = FakeResponseGenerator.redirect(
        url = "https://photos.example.com/login"
      )

      val actual = server.receive(request)

      actual must beEqualTo(expected)
    }

    "requests Jane to sign in using her username and password" in {

      val request = FakeRequestGenerator.get(
        scheme = "https",
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

    // (Not defined in the OAuth spec.)
    "make Jane logged in" in {

      val request = FakeRequestGenerator.post(
        scheme = "https",
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
        scheme = "https",
        host = photosWebSite,
        path = "/authorize"
      )

      val expected = FakeResponseGenerator.ok(
        contentType = "text/html",
        body = "A html content typically contains a set of buttons to authorize or deny access of 'printer.example.com'"
      )

      val actual = server.receive(request)

      actual must beEqualTo(expected)
    }

    "redirects Jane's user-agent to callback URI provided by the client" in {

      val request: Request = FakeRequestGenerator.post(
        scheme = "https",
        host = "photos.example.net",
        path = "/authorize"
      )

      val expected: Response = FakeResponseGenerator.redirect("http://printer.example.com/ready?oauth_token=hh5s93j4hdidpola&oauth_verifier=hfdp7dh39dks9884")

      val actual: Response = server.receive(request)

      actual must beEqualTo(expected)
    }

    "validates the request and and replies with a set of token credentials in the body of the HTTP response" in {

      val request = FakeRequestGenerator.post(
        scheme = "https",
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
