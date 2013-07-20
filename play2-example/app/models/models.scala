package models

import org.joda.time.DateTime
import play.api.libs.json.{JsValue, Json}
import java.net.{URLDecoder, URLEncoder, URI}

/**
 * Scope of authorizations to take control of accesses to protected resources
 *
 * @see http://tools.ietf.org/html/draft-ietf-oauth-v2-31#section-3.3
 */
sealed trait Scope {
   def value: Int
   def scope: String
}

trait ScopeDefinition {
  /**
   * @see http://tools.ietf.org/html/draft-ietf-oauth-v2-31#section-3.3
   * @see <code>0x21 :: (0x23 to 0x5b toList) ++ (0x5d to 0x7e) map ("\"" + _.toChar + "\"") mkString (", ")</code>
   */
  val allowedCharacters = Set(
    "!", "#", "$", "%", "&", "'", "(", ")", "*", "+", ",", "-", ".", "/",
    "0", "1", "2", "3", "4", "5", "6", "7", "8", "9", ":", ";", "<", "=", ">", "?", "@",
    "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q",
    "R", "S", "T", "U", "V", "W", "X", "Y", "Z",
    "[", "]", "^", "_", "`",
    "a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "m", "n", "o", "p", "q",
    "r", "s", "t", "u", "v", "w", "x", "y", "z",
    "{", "|", "}", "~"
  )
  def Values(scopes: Scope*): Map[String, Scope] = scopes.map(s => s.scope -> s).toMap
  def find(scope: String): Option[Scope] = values.get(scope)

  def values: Map[String, Scope]
}

/**
 * You may want to define more scopes.
 */
object Scope extends ScopeDefinition {

  case object Default extends Scope {
    val value = 0
    val scope = "default"
  }

  def values = Values(Default)
}

/**
 * Authorization code confirmed by user
 * @param code the authorization code
 * @param requestedScope the scope of this authorization code the client requested
 * @param authorizedScope the actual scope of this authorization code confirmed by user. this may differ from requestedScope.
 * @param redirectURI redirect_uri provided on the request
 */
case class Code(code: String, requestedScope: Scope, authorizedScope: Scope, redirectURI: Option[String] = None)

object Code {
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
  var codes = Map(
    "myCode" -> Code("myCode", Scope.Default, Scope.Default)
  )
  def generate(requestedScope: Scope, authorizedScope: Option[Scope], redirectURI: Option[String]): Code = {
    def newCode = {
      Code("newCode", requestedScope, authorizedScope.getOrElse(requestedScope), redirectURI)
    }
    save(newCode)
  }
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

case class AuthorizationRequest(
  responseType: String,
  clientId: Option[String],
  redirectURI: Option[String],
  scope: Option[String],
  status: Option[String]
)

case class Client(id: String, password: String, redirectionURI: Option[String] = None)

/**
 * The client service stub
 */
object Client {
  implicit def toMapped(client: Client) = new {
    def authorizedGrantTypes(implicit service: GrantTypeService = DefaultGrantTypeService): Seq[GrantType] = {
      service.findGrantTypes(client)
    }
    def addGrantType(grantType: GrantType)(implicit service: GrantTypeService = DefaultGrantTypeService) {
      service.addGrantType(client, grantType)
    }
  }
  var clients = Map("test" -> Client("test", "pass"))
  def find(id: String): Option[Client] = {
    clients.get(id)
  }
  def save(client: Client): Client = {
    clients ++= Map(client.id -> client)
    client
  }
}

/**
 *
 * @param accessToken
 * @param accessTokenType "Bearer"
 * @param refreshToken
 * @param expirationDate
 * @param scope
 */
case class Token(accessToken: String, accessTokenType: String, refreshToken: Option[String], expirationDate: DateTime, scope: Scope) {
  def expiresIn = (expirationDate.getMillis - new DateTime().getMillis) / 1000
  def isExpired = expiresIn <= 0
}

object Token {
  trait BaseParamBuilder {
    val token: Token

    /**
     * Build a successful access token response
     * See: http://tools.ietf.org/html/draft-ietf-oauth-v2-31#section-5.1
     * @param requestedScope
     * @return
     */
    def buildParamsMap(requestedScope: Scope): Map[String, Any] = {
      Map(
        "access_token" -> token.accessToken,
        "token_type" -> token.accessTokenType,
        "expires_in" -> token.expiresIn
      ) ++ token.refreshToken.map(t => Map("refresh_token" -> t)).getOrElse(Map.empty) ++
        (if (token.scope != requestedScope) Map("scope" -> token.scope.scope) else Map.empty)
    }
  }

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

  implicit def toURIComponentBuilder(_token: Token) = new BaseParamBuilder {
    val token = _token

    def buildURIComponent(requestedScope: Scope, state: Option[String]): String = {
      val params = buildParamsMap(requestedScope) ++ state.map(s => Map("state" -> s)).getOrElse(Map.empty)
      params.map { case (key, value) => Utils.uriEncode(key) + "=" + Utils.uriEncode(value.toString) }.mkString("&")
    }
  }

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
    Code.delete(code)
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
    (grantType, Code.find(code), Client.find(clientId)) match {
      case (grantType, _, _) if !supportedGrantTypes.contains(grantType) =>
        Left(UnsupportedGrantTypeError)
      case (_, None, _) =>
        Left(InvalidGrantError)
      case (_, _, None) =>
        Left(InvalidClientError)
      case (grantType, _, Some(client)) if !client.authorizedGrantTypes.map(_.grantType).contains(grantType) =>
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

object Utils {
  def isMalformed(uri: String) = try {
    new URI(uri)
    false
  } catch {
    case _ =>
    true
  }
  def uriEncode(str: String) = URLEncoder.encode(str, "utf-8")
  def uriDecode(str: String) = URLDecoder.decode(str, "utf-8")
}

sealed trait ResponseType {
  def asString: String
}

object ResponseType {

  private case class ResponseTypeImpl(asString: String) extends ResponseType

  private val CodeAsString = "code"
  private val TokenAsString = "token"

  def apply(str: String): ResponseType = str match {
    case CodeAsString => Code
    case TokenAsString => Token
  }

  def parseString(str: String): Either[Exception, ResponseType] = str match {
    case CodeAsString => Right(Code)
    case TokenAsString => Right(Token)
    case invalidStr => Left(new Exception("\"" + invalidStr + "\" is not a valid response type. \"" + CodeAsString + "\" or \"" + TokenAsString + "\" is valid."))
  }

  case object Code extends ResponseType {
    val asString = CodeAsString
  }

  case object Token extends ResponseType {
    val asString = TokenAsString
  }
}

trait GrantRequest {
  val client: Client
  val redirectionURI: String
  val requestedScope: Scope
  val state: Option[String]
  val responseType: ResponseType
}

case class ImplicitGrantRequest(client: Client, redirectionURI: String, requestedScope: Scope, state: Option[String]) extends GrantRequest {
  val responseType = ResponseType.Token
}

case class CodeGrantRequest(client: Client, redirectionURI: String, requestedScope: Scope, state: Option[String]) extends GrantRequest {
  val responseType = ResponseType.Code
}

case class AuthorizedGrantRequest(responseType: String, clientId: String, redirectionURI: String, requestedScope: String, authorizedScope: String, state: Option[String])

object OAuth2Provider {
  def validateCode(clientId: String, redirectURI: Option[String], requestedScope: Option[String], state: Option[String]): Either[TokenError, CodeGrantRequest] = {
    (Client.find(clientId), redirectURI, requestedScope, requestedScope.flatMap(Scope.find)) match {
      case (None, _, _, _) =>
        Left(InvalidClientError)
      case (Some(Client(_, _, Some(clientRedirectURI))), Some(providedRedirectURI), _, _) if clientRedirectURI != providedRedirectURI =>
        Left(InvalidRequestError("redirect_uri mismatches."))
      case (Some(Client(_, _, None)), None, _, _) =>
        Left(InvalidRequestError("redirect_uri missing."))
      case (Some(Client(_, _, _)), Some(redirectURI), _, _) if Utils.isMalformed(redirectURI) =>
        Left(InvalidRequestError("redirect_uri is malformed."))
      case (Some(client), redirectionURI, scope, validScope) =>
        val redirectURI = client.redirectionURI.orElse(redirectionURI)
        if (scope.isDefined && validScope.isEmpty) {
          Left(InvalidRequestError("Undefined scope: " +  scope.get + " (length: " + scope.get.size + ")", redirectURI))
        } else {
          val requestedScope = validScope.getOrElse(Scope.Default)
          Right(
            CodeGrantRequest(client, redirectURI.get, requestedScope, state)
          )
        }
    }
  }

  // http://tools.ietf.org/html/draft-ietf-oauth-v2-31#section-4.2.1
  def validateImplicit(clientId: String, redirectURI: Option[String], requestedScope: Option[String], state: Option[String]): Either[TokenError, ImplicitGrantRequest] = {
    (Client.find(clientId), redirectURI, requestedScope, requestedScope.flatMap(Scope.find)) match {
      case (None, _, _, _) =>
        Left(InvalidClientError)
      case (Some(Client(_, _, Some(clientRedirectURI))), Some(providedRedirectURI), _, _) if clientRedirectURI != providedRedirectURI =>
        Left(InvalidRequestError("redirect_uri mismatches."))
      case (Some(Client(_, _, None)), None, _, _) =>
        Left(InvalidRequestError("redirect_uri missing."))
      case (Some(Client(_, _, _)), Some(redirectURI), _, _) if Utils.isMalformed(redirectURI) =>
        Left(InvalidRequestError("redirect_uri is malformed."))
      case (Some(client), redirectionURI, scope, validScope) =>
        val redirectURI = client.redirectionURI.orElse(redirectionURI)
        if (scope.isDefined && validScope.isEmpty) {
          Left(InvalidRequestError("Undefined scope: " +  scope.get + " (length: " + scope.get.size + ")", redirectURI))
        } else {
          val requestedScope = validScope.getOrElse(Scope.Default)
          Right(
            ImplicitGrantRequest(client, redirectURI.get, requestedScope, state)
          )
        }
    }
  }
  /**
   * Returns an error or a successful redirect URI.
   * @param clientId
   * @param redirectURI
   * @param authorizedScope User authorized scope
   * @return
   */
  def grantImplicit(clientId: String, redirectURI: String, requestedScope: String, authorizedScope: String, state: Option[String]): Either[server.AuthzServerError, String] = {
    (Client.find(clientId), redirectURI, Scope.find(requestedScope), Scope.find(authorizedScope)) match {
      case (None, _, _, _) =>
        Left(server.ClientNotFoundError)
      case (_, _, None, _) =>
        Left(server.InvalidRequestError("Undefined scope: " + requestedScope))
      case (_, _, _, None) =>
        Left(server.InvalidRequestError("Undefined scope: " + authorizedScope))
      case (Some(client), redirectionURI, Some(requestedScope), Some(authorizedScope)) =>
        val token = Token.issue(authorizedScope)
        Right(redirectionURI + "#" + token.buildURIComponent(requestedScope, state))
    }
  }
}

package server {
  sealed trait AuthzServerError
  object ClientNotFoundError extends AuthzServerError
  case class InvalidRequestError(description: String) extends AuthzServerError
}

sealed trait TokenError {
  val error: String
  val errorDescription: Option[String] = None
  val redirectURI: Option[String] = None
  val errorURI: Option[String] = None
  val state: Option[String] = None

  def buildParamsMap: Map[String, Any] = {
    Map(
      "error" -> error
    ) ++ errorDescription.map(d => Map("error_description" -> d)).getOrElse(Map.empty) ++
    errorURI.map(u => Map("error_uri" -> u)).getOrElse(Map.empty) ++
    state.map(s => Map("state" -> s)).getOrElse(Map.empty)
  }

  def buildResponse: JsValue = {
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

  def buildRedirectionURI: Option[String] = {
    redirectURI.map(u => u + "#" + buildParamsMap.map { case (key, value) => Utils.uriEncode(key) + "=" + Utils.uriEncode(value.toString) }.mkString("&"))
  }
}

object InvalidClientError extends TokenError {
  val error = "invalid_client"
}

object InvalidGrantError extends TokenError {
  val error = "invalid_grant"
}

object UnauthorizedClientError extends TokenError {
  val error = "unauthorized_client"
}

object UnsupportedGrantTypeError extends TokenError {
  val error = "unsupported_grant_type"
}

object InvalidScopeError extends TokenError {
  val error = "invalid_scope"
}

object RedirectURIError extends TokenError {
  val error = "hoge"
}

case class InvalidRequestError(description: String, override val redirectURI: Option[String] = None) extends TokenError {
  val error = "invalid_request"
  override val errorDescription = Some(description)
}

sealed trait GrantType {
  def grantType: String
  def asString = grantType
}

object GrantType {

  private val AuthorizationCodeAsString = "authorization_code"
  private val TokenAsString = "token"

  def apply(str: String): GrantType = str match {
    case AuthorizationCodeAsString => Code
    case TokenAsString => Token
  }

  case object Code extends GrantType {
    val grantType = AuthorizationCodeAsString
  }

  case object Token extends GrantType {
    val grantType = TokenAsString
  }
}

trait GrantTypeService {
  def addGrantType(client: Client, grantType: GrantType): Unit
  def findGrantTypes(client: Client): Seq[GrantType]
}
object DefaultGrantTypeService extends GrantTypeService {
  def addGrantType(client: Client, grantType: GrantType) = {

  }
  def findGrantTypes(client: Client): Seq[GrantType] = {
    Seq(GrantType.Code, GrantType.Token)
  }
}

case class User(id: String, secret: String)

/**
 * The user service stub
 */
object User {
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
