package models

import org.joda.time.DateTime
import play.api.libs.json.{JsValue, Json}
import oauth2._
import oauth2.error._
import oauth2.entity.{Code, Token}
import oauth2.service.{CodeService, TokenService, AuthorizationService}
import oauth2.definition.ScopeDefinition
import oauth2.value_object.Scope

object ScopeDef extends ScopeDefinition {

  /**
   * You may want to define more scopes.
   */
  case object Default extends Scope {
    val value = 0
    val scope = "default"
  }

  def values = Values(Default)
}

object CodeSvc extends CodeService {
  var codes = Map(
    "myCode" -> Code("myCode", ScopeDef.Default, ScopeDef.Default)
  )

  def find(code: String): Option[Code] = codes.get(code)

  def delete(code: Code) {
    // TODO
  }
  def save(code: Code): Code = {
    codes ++= Map(code.code -> code)
    play.api.Logger.info("codes: " + codes)
    code
  }
}

object TokenSvc extends TokenService {

  class ParamBuilder(val token: Token) extends BaseParamBuilder {
    /**
     * Build a successful access token response
     * See: http://tools.ietf.org/html/draft-ietf-oauth-v2-31#section-5.1
     * @param requestedScope
     * @return
     */
    def buildResponse(requestedScope: Scope): JsValue = {
      Json.toJson(
        buildParamsMap(requestedScope).map { case (key, value) =>
          val v = value match {
            case v: Long => Json.toJson(v)
            case v: String => Json.toJson(v)
          }
          key -> v
        }
      )
    }
  }

  implicit def toParamBuilder(token: Token) = new ParamBuilder(token)

  var tokens = Map.empty[String, Token]

  def find(token: String) = tokens.get(token)

  def save(token: Token) = {
    tokens ++= Map(token.accessToken -> token)
    token
  }

  def issue(code: Code): (Scope, Token) = {
    val accessToken = "testToken"
    val accessTokenType = "Bearer"
    val refreshToken = None
    val expiresIn = 3600
    CodeSvc.delete(code)
    val scope = code.authorizedScope
    (code.requestedScope, save(Token(accessToken, accessTokenType, refreshToken, new DateTime().plusSeconds(expiresIn), scope)))
  }

  def issue(authorizedScope: Scope): Token = {
    val accessToken = "testToken"
    val accessTokenType = "Bearer"
    val refreshToken = None
    val expiresIn = 3600
    save(Token(accessToken, accessTokenType, refreshToken, new DateTime().plusSeconds(expiresIn), authorizedScope))
  }

  /**
   * Issue an access token in Authorization Code Flow
   *
   * See: Access Token Request
   * http://tools.ietf.org/html/draft-ietf-oauth-v2-31#section-4.1.3
   *
   * May return an authorization error according to
   * <a href="http://tools.ietf.org/html/draft-ietf-oauth-v2-31#section-5.2">Section 5.2</a>
   * @param grantType should be "code"
   * @param code should be a valid authorization code
   * @param clientId should be a valid client id
   * @return
   */
  def issue(grantType: String, code: String, clientId: String, redirectURI: Option[String]): Either[TokenError, (Scope, Token)] = {
    val supportedGrantTypes = List("authorization_code")
    (grantType, CodeSvc.find(code), ClientSvc.find(clientId)) match {
      case (grantType, _, _) if !supportedGrantTypes.contains(grantType) =>
        Left(UnsupportedGrantTypeError)
      case (_, None, _) =>
        Left(InvalidGrantError)
      case (_, _, None) =>
        Left(InvalidClientError)
      case (grantType, _, Some(client)) if !ClientSvc.toMapped(client).authorizedGrantTypes(ClientGrantTypeSvc).map(_.grantType).contains(grantType) =>
        Left(UnauthorizedClientError)
      case (grantType, Some(code), Some(client)) if code.redirectURI != redirectURI =>
        // redirectURI's must be identical if provided on the initial authorization request.
        // But it is not defined that what response we should/must send to the client when a mismatch happen.
        // Here, I send an InvalidGrantError anyway.
        play.api.Logger.info("InvalidGrantError: Mismatched redirectURIs " + code.redirectURI + " and " + redirectURI)
        Left(InvalidGrantError)
      case (grantType, Some(code), Some(client)) =>
        Right(
          issue(code)
        )
    }
  }


}

object AuthorizationSvc extends AuthorizationService(
  ClientSvc,
  TokenSvc,
  ScopeDef,
  Some(ScopeDef.Default)
)

object Implicits {
  implicit val clientGrantTypeSvc = ClientGrantTypeSvc
  implicit val tokenErrorResponseBuilder = new TokenErrorResponseBuilder[JsValue] {
    def buildResponse(buildParamsMap: Map[String, Any]): JsValue = {
      Json.toJson(
        buildParamsMap.map {
          case (key, value) =>
            val v = value match {
              case v: Long => Json.toJson(v)
              case v: String => Json.toJson(v)
            }
            key -> v
        }
      )
    }

  }
}

case class User(id: String, secret: String)

/**
 * The user service stub
 */
object UserSvc {
  var users = Map("testuser" -> User("testuser", "pass"))
  def authenticate(id: String, password: String): Option[User] = {
    users.get(id) match {
      case Some(user) if user.secret == password =>
        Some(user)
      case _ =>
        None
    }
  }
}
