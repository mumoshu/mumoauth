package com.github.mumoshu.mumoauth

trait Request {
  def method: String

  def authority: String = ""

  def path: String

  def queryString: Option[String]

  def oauthParameters: String = ""

  def authorization: Option[String]

  def contentType: Option[String]

  def messageBody: Option[String]

  def entityBody = messageBody

  def scheme: String

  def port: Int

  def secure: Boolean = scheme.toLowerCase == "https"

  // From the HOST header field
  def host: String

  def httpVersion: String

  // http://tools.ietf.org/html/rfc5849#section-3.4.1.2
  def baseStringURI: String = {
    scheme.toLowerCase + "://" + host.toLowerCase + {
      if (secure && port != 443 || !secure && port != 80) port else ""
    } + path + queryString
  }

  def oauthSignature: Option[String] = {
    BaseString.authorizationHeaderParametersOf(this).find(_._1 == "oauth_signature").map(_._2)
  }

  def oauthClientIdentifier: Option[String] = {
    BaseString.authorizationHeaderParametersOf(this).find(_._1 == "oauth_consumer_key").map(_._2)
  }

  def oauthTemporaryOrTokenIdentifier: Option[String] = {
    BaseString.authorizationHeaderParametersOf(this).find(_._1 == "oauth_token").map(_._2)
  }

  def concat = method.toString.toUpperCase + "&" + baseStringURI + "&" + ""
}
