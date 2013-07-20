package oauth2

/**
 * Authorization code confirmed by user
 * @param code the authorization code
 * @param requestedScope the scope of this authorization code the client requested
 * @param authorizedScope the actual scope of this authorization code confirmed by user. this may differ from requestedScope.
 * @param redirectURI redirect_uri provided on the request
 */
case class Code(code: String, requestedScope: Scope, authorizedScope: Scope, redirectURI: Option[String] = None)

trait CodeDefinition {
  implicit def toURIComponentBuilder(code: Code) = new {
    def parsedQueryParameterMap(state: Option[String]): Map[String, Seq[String]] = {
      val nameValuePairs = "code" -> code.code ::
        List(code.authorizedScope.scope).filter(code.requestedScope !=).map("scope" ->) ++
        state.map("state" ->)

      { nameValuePairs toMap } mapValues { Seq(_) }
    }

//    def parsedQueryParameterMap(state: Option[String]): Map[String, Seq[String]] =
//      Map(
//        "code" -> code.code
//      ) ++ (if (code.authorizedScope != code.requestedScope) Map("scope" -> code.authorizedScope) else Map.empty) ++
//        state.map(s => Map("state" -> s)).getOrElse(Map.empty)
    def buildURIComponent(state: Option[String]): String = {
      val components = for {
        (name, values) <- parsedQueryParameterMap(state)
        v <- values
      } yield Utils.uriEncode(name) + "=" + Utils.uriEncode(v)

      components mkString "&"
    }
  }
  def generate(requestedScope: Scope, authorizedScope: Option[Scope], redirectURI: Option[String]): Code = {
    def newCode = {
      Code("newCode", requestedScope, authorizedScope.getOrElse(requestedScope), redirectURI)
    }
    save(newCode)
  }

  def find(code: String): Option[Code]
  def save(code: Code): Code
}
