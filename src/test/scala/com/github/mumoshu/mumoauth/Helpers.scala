package com.github.mumoshu.mumoauth

import server.{ ResponseGenerator, RequestGenerator }
import test.FakeResponse

object Helpers {

  object FakeRequestGenerator extends RequestGenerator {

    private def defaultPortFor(scheme: String): Int = if (scheme == "http") 80 else if (scheme == "https") 443 else throw new Exception("I don't know the defualt port for scheme %s".format(scheme))

    def post(scheme: String, path: String, host: String, port: Option[Int] = None, contentType: Option[String] = None, body: Option[String] = None, authorization: Option[String] = None) = FakeRequest(
      method = "POST",
      port = port.getOrElse(defaultPortFor(scheme)),
      scheme = scheme,
      path = path,
      host = host,
      contentType = contentType,
      messageBody = body,
      authorization = authorization
    )

    def get(scheme: String, path: String, queryString: Option[String] = None, host: String, port: Option[Int] = None) = FakeRequest(
      scheme = scheme,
      method = "GET",
      path = path,
      queryString = queryString,
      host = host,
      port = port.getOrElse(defaultPortFor(scheme))
    )
  }

  object FakeResponseGenerator extends ResponseGenerator {

    def ok(contentType: String, body: String) = FakeResponse(
      status = 200,
      contentType = Some(contentType),
      body = Some(body)
    )

    def redirect(url: String, setCookie: Option[String] = None) = FakeResponse(
      status = 302
    )
  }
}
