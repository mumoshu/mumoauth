package com.github.mumoshu.mumoauth.test

import com.github.mumoshu.mumoauth.server.ResponseGenerator
import com.github.mumoshu.mumoauth.Response

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
