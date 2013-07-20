package com.github.mumoshu.mumoauth

import org.specs2.mutable.Specification

import Helpers.FakeRequestGenerator

/**
 * See "The OAuth 1.0 Protocol"
 * @see http://tools.ietf.org/html/rfc5849
 */
object SignatureSpec extends Specification {

  "HMAC-SHA1 signature method" should {

    import BaseString.percentDecodeOf

    val req = FakeRequest(
      scheme = "http",
      port = 80,
      method = "POST",
      path = "/request",
      queryString = Some("?b5=%3D%253D&a3=a&c%40=&a2=r%20b"),
      httpVersion = "1.1",
      host = "example.com",
      authorization = Some("""OAuth realm="Example",oauth_consumer_key="9djdj82h48djs9d2",oauth_token="kkk9d7dh3k39sjv7",oauth_signature_method="HMAC-SHA1",oauth_timestamp="137131201",oauth_nonce="7d8f3e4a",oauth_signature="bYT5CMsGcbgUdFHObYMEfcx6bsw%3D""""),
      contentType = Some("application/x-www-form-urlencoded"),
      messageBody = Some("c2&a3=2+q")
    )

    println("Base String: " + BaseString.baseStringOf(req))

    "produce a valid signature for the example request given in 3.4.1.1 String Construction" in {

      // The the oauth_signature in the OAuth spec is wrong...!
      // See Re: [OAUTH-WG] Fwd: [Technical Errata Reported] RFC5849 (2550)
      // http://www.ietf.org/mail-archive/web/oauth/current/msg07886.html
      // val expectedPercentEncoded = "bYT5CMsGcbgUdFHObYMEfcx6bsw%3D"

      val expectedPercentEncoded = "r6%2FTJjbCOr97%2F%2BUU0NsvSne7s5g%3D"

      // This example request is also used in "3.1.  Making Requests"
      BaseString.percentEncodeOf(HMACSHA1("j49sk3j29djd", "dh893hdasih9").sign(BaseString.baseStringOf(req))) must beEqualTo(expectedPercentEncoded)
    }

    "produce a valid signature for the Temporary Credential Request" in {

      val request = FakeRequestGenerator.post(
        scheme = "https",
        path = "/initiate",
        host = "photos.example.net",
        authorization = Some("""OAuth realm="Photos",oauth_consumer_key="dpf43f3p2l4k3l03",oauth_signature_method="HMAC-SHA1",oauth_timestamp="137131200",oauth_nonce="wIjqoS",oauth_callback="http%3A%2F%2Fprinter.example.com%2Fready",oauth_signature="74KNZJeDHnMBp0EMJ9ZHt%2FXKycU%3D"""")
      )

      HMACSHA1("kd94hf93k423kf44", "").sign(BaseString.baseStringOf(request)) must beEqualTo(percentDecodeOf("74KNZJeDHnMBp0EMJ9ZHt%2FXKycU%3D"))
    }

    "produce a valid signature for the Token Request" in {

      val request = FakeRequestGenerator.post(
        scheme = "https",
        host = "photos.example.net",
        path = "/token",
        authorization = Some("""OAuth realm="Photos",oauth_consumer_key="dpf43f3p2l4k3l03",oauth_token="hh5s93j4hdidpola",oauth_signature_method="HMAC-SHA1",oauth_timestamp="137131201",oauth_nonce="walatlh",oauth_verifier="hfdp7dh39dks9884",oauth_signature="gKgrFCywp7rO0OXSjdot%2FIHF7IU%3D"""")
      )

      HMACSHA1("kd94hf93k423kf44", "hdhd0244k9j7ao03").sign(BaseString.baseStringOf(request)) must beEqualTo(percentDecodeOf("gKgrFCywp7rO0OXSjdot%2FIHF7IU%3D"))
    }
  }

}
