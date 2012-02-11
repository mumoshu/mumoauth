package com.github.mumoshu.mumoauth

import com.github.mumoshu.mumoauth.BaseString

/**
 * OAuth defines 3 signature methods used to sign and verify requests
 */
trait SignatureMethod {
  def sign(text: String): String
}

trait PublicKey {
  //  def verify(r: SignedRequest): Request
}

// Used by RSA-SHA-1 to sign a request.
trait PrivateKey {
  //  def sign(r: Request): SignedRequest
}

// Client signs a request by RSA-SHA-1
case class RSASHA1(secret: PrivateKey) extends SignatureMethod {
  def sign(text: String) = ""
}

case class RSASHA1Verifier(secret: PublicKey) {
  def verify(text: String) = ""
}

case class PLAINTEXT(clientSecret: String, tokenSecret: String) extends SignatureMethod {
  def sign(text: String) = ""
}

case class HMACSHA1(clientSecret: String, tokenSecret: String) extends SignatureMethod {
  def sign(text: String) = {
    import BaseString.percentEncodeOf

    def encodeBASE64(bytes: Array[Byte]): String = {
      ""
    }

    def hmacSha1(key: String, text: String): Array[Byte] = {
      Array.empty
    }

    val key = percentEncodeOf(clientSecret) + "&" + percentEncodeOf(tokenSecret)

    encodeBASE64(hmacSha1(key, text))
  }
}
