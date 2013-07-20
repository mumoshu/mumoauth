package oauth2

trait TokenErrorResponseBuilder[A] {
  def buildResponse(buildParamsMap: Map[String, Any]): A
}
