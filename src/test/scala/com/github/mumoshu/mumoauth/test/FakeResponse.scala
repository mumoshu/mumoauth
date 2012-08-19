package com.github.mumoshu.mumoauth.test

import com.github.mumoshu.mumoauth.Response

case class FakeResponse(
  status: Int,
  contentType: Option[String] = None,
  body: Option[String] = None,
  location: Option[String] = None,
  httpVersion: String = "1.1") extends Response
