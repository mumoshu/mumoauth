package oauth2.service

import oauth2.entity.Code
import oauth2.value_object.Scope
import oauth2.Utils

trait CodeService {
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
