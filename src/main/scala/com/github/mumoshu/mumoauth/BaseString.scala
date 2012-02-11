package com.github.mumoshu.mumoauth

import com.github.mumoshu.mumoauth.Request

object BaseString {

  def hex(s: String) = Integer.toHexString(s.charAt(0).toInt)

  def percentEncodeSingle(str: String): String = {
    val p = "[a-zA-Z0-9-._~]".r
    p.findFirstIn(str).getOrElse("%" + hex(str).toUpperCase)
  }

  // 3.6.  Percent Encoding
  def percentEncodeOf(str: String) =
    str.split("").filter(_ != "").map(percentEncodeSingle).mkString("")
  //    java.net.URLEncoder.encode(str, "UTF-8")
  //      // OAuth encodes some characters differently:
  //      .replace("+", "%20").replace("*", "%2A")
  //      .replace("%7E", "~")

  def percentDecodeOf(str: String): String = {
    val p = "%([A-F0-9]{2})".r
    p.replaceAllIn(
      str,
      m => Integer.parseInt(m.group(1), 16).toChar.toString.replaceAll("\\\\", "\\\\\\\\").replaceAll("\\$", "\\\\\\$")
    )
    //    java.net.URLDecoder.decode(str, "UTF-8")
  }

  private def urlDecode(encoded: String) = java.net.URLDecoder.decode(encoded, "UTF-8")

  // 3.4.1.3.1.  Parameter Sources
  def parametersOf(req: Request): Array[(String, String)] = {

    val queryComponents = queryComponentsOf(req)

    val authorizationHeaderParameters = authorizationHeaderParametersOf(req, List("realm", "oauth_signature"))

    val entityBodyParameters = if (req.contentType == Some("application/x-www-form-urlencoded"))
      req.entityBody.getOrElse("").split("&").foldLeft(Array.empty[(String, String)]) {
        (ary, keyValuePair) =>
          keyValuePair.split("=").map(urlDecode) match {
            case Array(key, value) => ary ++ Array(key -> value)
            case Array(key) => ary ++ Array(key -> "")
            case _ => throw new RuntimeException("Could not parse %s".format(keyValuePair))
          }
      }
    else
      Array.empty[(String, String)]

    queryComponents ++ authorizationHeaderParameters ++ entityBodyParameters
  }

  // 3.4.1.3.2.  Parameters Normalization
  def normalizedParametersOf(req: Request) =
    parametersOf(req).map(t => percentEncodeOf(t._1) -> percentEncodeOf(t._2)).sortBy(t => t).map(t => t._1 + "=" + t._2).mkString("&")

  // 3.4.1.1.  String Construction
  def baseStringOf(req: Request) = req.method.toUpperCase + "&" + percentEncodeOf(baseStringURIOf(
    req.scheme, req.method, req.path, req.host, req.httpVersion, req.port
  )) + "&" + percentEncodeOf(normalizedParametersOf(req))

  def baseStringURIOf(scheme: String, method: String, path: String, host: String, httpVersion: String, port: Int) = {
    val schemeInLowerCase = scheme.toLowerCase
    schemeInLowerCase + "://" + host.toLowerCase + (if (schemeInLowerCase == "http" && port != 80 || schemeInLowerCase == "https" && port != 443) {
      ":" + port
    } else "") + path
  }

  def queryComponentsOf(req: Request): Array[(String, String)] = {
    req.queryString match {
      case Some(q) => q.replaceFirst("\\?", "").split("&").foldLeft(Array.empty[(String, String)]) {
        (ary, keyValuePair) =>
          keyValuePair.split("=").map(urlDecode) match {
            case Array(key, value) => ary ++ Array(key -> value)
            case Array(key) if keyValuePair.endsWith("=") => ary ++ Array(key -> "")
            case _ => throw new RuntimeException("Could not match %s".format(keyValuePair))
          }
      }
      case _ => Array.empty
    }
  }

  def authorizationHeaderParametersOf(req: Request, excludes: List[String] = Nil): Array[(String, String)] = {

    val p = "([^ ,]+)=\"([^\"]*)".r

    p.findAllIn(req.authorization.getOrElse("").replaceFirst("OAuth", "")).matchData.map(_.subgroups).map(groups => groups.map(percentDecodeOf)).foldLeft(Array.empty[(String, String)]) {
      case (ary, List(key, value)) if !excludes.contains(key) =>
        ary ++ Array(key -> value)
      case (ary, List(_, _)) => ary
      case _ =>
        throw new RuntimeException("Could not match something...")
    }
  }

}
