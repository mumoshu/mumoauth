package com.github.mumoshu.mumoauth

import org.specs2.mutable._

object BaseStringSpec extends Specification {

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

  "The base string of the example HTTP request in OAuth spec" should {

    // 3.4.1.1.  String Construction
    "match the base string indicated in the spec" in {
      import BaseString.baseStringOf

      baseStringOf(req) must beEqualTo("""POST&http%3A%2F%2Fexample.com%2Frequest&a2%3Dr%2520b%26a3%3D2%2520q%26a3%3Da%26b5%3D%253D%25253D%26c%2540%3D%26c2%3D%26oauth_consumer_key%3D9djdj82h48djs9d2%26oauth_nonce%3D7d8f3e4a%26oauth_signature_method%3DHMAC-SHA1%26oauth_timestamp%3D137131201%26oauth_token%3Dkkk9d7dh3k39sjv7""")
    }
  }

  // 3.4.1.2.  Base String URI
  "The base string URI of the example" should {

    "match the one for http indicated in the spec" in {
      import BaseString.baseStringURIOf

      val baseStringURI = baseStringURIOf(scheme = "http", method = "GET", path = "/r%20v/X", httpVersion = "1.1", host = "EXAMPLE.COM", port = 80)

      baseStringURI must beEqualTo("http://example.com/r%20v/X")
    }

    "match the one for https indicate in in the spec" in {
      import BaseString.baseStringURIOf

      val baseStringURI = baseStringURIOf(scheme = "https", method = "GET", path = "/", port = 8080, httpVersion = "1.1", host = "www.example.net")

      baseStringURI must beEqualTo("https://www.example.net:8080/")
    }
  }

  // 3.4.1.3.1.  Parameter Sources
  "req" should {

    "contains parameters used in the signature base string" in {

      import BaseString.parametersOf

      val parameters = parametersOf(req)

      parameters must beEqualTo(Array(
        "b5" -> "=%3D",
        "a3" -> "a",
        "c@" -> "",
        "a2" -> "r b",
        "oauth_consumer_key" -> "9djdj82h48djs9d2",
        "oauth_token" -> "kkk9d7dh3k39sjv7",
        "oauth_signature_method" -> "HMAC-SHA1",
        "oauth_timestamp" -> "137131201",
        "oauth_nonce" -> "7d8f3e4a",
        "c2" -> "",
        "a3" -> "2 q"
      ))
    }

    "converted to normalized parameters" in {

      import BaseString.normalizedParametersOf

      normalizedParametersOf(req) must beEqualTo("a2=r%20b&a3=2%20q&a3=a&b5=%3D%253D&c%40=&c2=&oauth_consumer_key=9djdj82h48djs9d2&oauth_nonce=7d8f3e4a&oauth_signature_method=HMAC-SHA1&oauth_timestamp=137131201&oauth_token=kkk9d7dh3k39sjv7")
    }
  }

  // 3.4.1.3.2.  Parameters Normalization
  // No tests yet.

  // 3.6.  Percent Encoding
  "percent encoding" should {

    import BaseString.{ percentEncodeOf, percentDecodeOf }

    "should properly percent encode" in {

      val unreservedCharacters = "abcdefghijlmnopqrstuvwxyz0123456789-._~"

      percentEncodeOf(unreservedCharacters) must beEqualTo(unreservedCharacters)

      val reservedCharacters = """!\"#$%&'()=^|¥[{@`]}:*,<>/?"""

      percentEncodeOf(reservedCharacters) must beEqualTo("%21%5C%22%23%24%25%26%27%28%29%3D%5E%7C%A5%5B%7B%40%60%5D%7D%3A%2A%2C%3C%3E%2F%3F")

      percentDecodeOf(percentEncodeOf(reservedCharacters + unreservedCharacters)) must beEqualTo(reservedCharacters + unreservedCharacters)
    }

    "should properly decode" in {

      percentDecodeOf("abcdefg") must beEqualTo("abcdefg")

      percentDecodeOf(percentEncodeOf("#$%&'()0")) must beEqualTo("#$%&'()0")

      percentDecodeOf("http%3A%2F%2Fprinter.example.com%2Fready") must beEqualTo("http://printer.example.com/ready")
    }
  }

}
