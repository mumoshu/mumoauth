package controllers

import play.api.data._
import play.api.data.Forms._
import format.Formats._
import play.api.mvc._
import org.joda.time.DateTime
import play.api.libs.json.{JsValue, Json}
import models._
import models.InvalidRequestError
import models.AuthorizationRequest
import scala.Some
import models.AuthorizedGrantRequest
import play.api.libs.ws.WS
import org.apache.commons.codec.binary.Base64

/**
 * The OAuth2 authorization server
 * - ask the resource owner for authorizations to access protected resources (`authorize` action represents the Authorization endpoint defined in OAuth2 spec)
 * - issue access tokens to clients (`token` action represents the Token endpoint defined in OAuth2 spec)
 */
object ServerController extends Controller {
  
  def index = Action {
    Ok(views.html.index("Your new application is ready."))
  }


  def authorizePost = Action { implicit request =>
    val f = request.body.asFormUrlEncoded.get
    authorize(
      f.get("response_type").get.head,
      f.get("client_id").map(_.head),
      f.get("redirect_uri").map(_.head),
      f.get("scope").map(_.head),
      f.get("state").map(_.head)
    )(request)
  }

  val authorizedGrantRequestForm = Form(
    mapping(
      "grant_type" -> text,
      "client_id" -> text,
      "redirect_uri" -> text,
      "requested_scope" -> text,
      "authorized_scope" -> text,
      "state" -> optional(text)
    )(AuthorizedGrantRequest.apply)(AuthorizedGrantRequest.unapply)
  )

  /**
   * Authorization endpoint
   * @param response_type
   * @param client_id
   * @param redirect_uri
   * @param scope
   * @param state
   * @return
   */
  def authorize(response_type: String, client_id: Option[String], redirect_uri: Option[String], scope: Option[String], state: Option[String]) = Action {
    (response_type, client_id, redirect_uri, scope, state) match {
      case ("code", Some(clientId), redirectURI, scope, state) =>
        // Authorization code flow
        // http://tools.ietf.org/html/draft-ietf-oauth-v2-31#section-4.1
        //
        // Authorizaton request
        // http://tools.ietf.org/html/draft-ietf-oauth-v2-31#section-4.1.1
        OAuth2Provider.validateCode(clientId, redirectURI, scope, state).fold[Result](
          e => e.buildRedirectionURI.map(Redirect(_)).getOrElse(BadRequest(e.toString)),
          r => Ok(views.html.authorize(authorizedGrantRequestForm.fill(AuthorizedGrantRequest("code", r.client.id, r.redirectionURI, r.requestedScope.scope, r.requestedScope.scope, r.state))))
        )
      case ("token", Some(clientId), redirectURI, scope, state) =>
        // Implicit grant
        // http://tools.ietf.org/html/draft-ietf-oauth-v2-31#section-4.2
        //
        // Authorization request
        // http://tools.ietf.org/html/draft-ietf-oauth-v2-31#section-4.2.1
        OAuth2Provider.validateImplicit(clientId, redirectURI, scope, state).fold[Result](
          e => e.buildRedirectionURI.map(Redirect(_)).getOrElse(BadRequest(e.toString)),
          r => Ok(views.html.authorize(authorizedGrantRequestForm.fill(AuthorizedGrantRequest("token", r.client.id, r.redirectionURI, r.requestedScope.scope, r.requestedScope.scope, r.state))))
        )
      case (reqType, _, _, _, _) =>
        val e = InvalidRequestError("Invalid request_type: " + reqType)
        e.buildRedirectionURI.map(Redirect(_)).getOrElse(BadRequest(e.toString))
    }
  }

  def code = Action { implicit request =>
    authorizedGrantRequestForm.bindFromRequest.fold(
      e => BadRequest(e.toString),
      f => f.grantType match {
        case "code" =>
          Scope.find(f.authorizedScope) match {
            case Some(authorizedScope) =>
              Redirect(
                f.redirectionURI + "?" + Code.generate(Scope.find(f.requestedScope).get, Some(authorizedScope), Some(f.redirectionURI)).buildURIComponent(f.state)
              )
            case None =>
              throw new RuntimeException("Unexpected scope: " + f.authorizedScope)
          }
        case "token" =>
          Scope.find(f.authorizedScope) match {
            case Some(authorizedScope) =>
              Redirect(
                f.redirectionURI + "#" + Token.issue(authorizedScope).buildURIComponent(Scope.find(f.requestedScope).get, f.state)
              )
            case None =>
              throw new RuntimeException("Unexpected scope: " + f.authorizedScope)
          }
      }
    )
  }

  /**
   * Token endpoint
   * @return
   */
  def token = Action { implicit request =>

  /**
   * 3.2.1 Client Authentication
   * http://tools.ietf.org/html/draft-ietf-oauth-v2-31#section-3.2.1
   */
    def basicAuthenticatedClient(implicit request: Request[AnyContent]) = request.headers.get("Authorization").get.split(" ") match {
      case Array(scheme, userpass) if scheme == "Basic" =>
        new String(Base64.decodeBase64(userpass), "utf-8").split(":")match {
          case Array(user, pass) =>
            Client.find(user) match {
              case Some(client) if client.password == pass =>
                Some(client)
              case _ =>
                //Unauthorized("username or password does not exist")
                None
            }
        }
      case Array(scheme, _) =>
        //                Unauthorized("Unknown authorization scheme: " + scheme)
        None
      case unexpected =>
        throw new RuntimeException("Unexpected Authorization header: " + unexpected)
    }
    def basicAuthenticatedClientId(implicit request: Request[AnyContent]) = basicAuthenticatedClient.map(_.id)

    val form = Form(
      tuple(
        "grant_type" -> text,
        "code" -> text,
        "redirect_uri" -> text,
        "client_id" -> optional(text),
        "username" -> optional(text),
        "password" -> optional(text),
        "scope" -> optional(text)
      )
    )
    form.bindFromRequest.fold(
      f => BadRequest(f.toString),
      t => {
        t match {
          // 4.1 Authorization Code Grant
          // 4.1.3 Access Token Request
          // http://tools.ietf.org/html/draft-ietf-oauth-v2-31#section-4.1.3
          case (grant_type, code, redirectURI, clientId, _, _, _) if grant_type == "authorization_code" =>

            val client = clientId.orElse(basicAuthenticatedClientId)
            client match {
              case Some(client) =>
                Token.issue(grant_type, code, client, Some(redirectURI)).fold(
                  e => BadRequest(e.buildResponse), {
                  case (requestedScope, token) => accessTokenResult(token, requestedScope)
                })
              case _ =>
                Unauthorized(InvalidClientError.buildResponse)
            }

          // 4.3 Resource Owner Password Credentials Grant
          // http://tools.ietf.org/html/draft-ietf-oauth-v2-31#section-4.3
          case ("password", _, _, _, Some(username), Some(password), scope) =>
            (request.headers.get("Authorization"), basicAuthenticatedClient, User.authenticate(username, password), scope, scope.flatMap(Scope.find)) match {
              case (Some(_), Some(client), Some(user), Some(scope), Some(validatedScope)) =>
                accessTokenResult(Token.issue(validatedScope), validatedScope)
              case (_, _, Some(user), None, None) =>
                val theScope = Scope.default
                accessTokenResult(Token.issue(theScope), theScope)
              case _ =>
                BadRequest(InvalidRequestError("invalid_request").buildResponse)
            }

          // 4.4 Client Credentials Grant
          // http://tools.ietf.org/html/draft-ietf-oauth-v2-31#section-4.4
          case ("client_credentials", _, _, _, _, _, scope) =>
            (basicAuthenticatedClient, scope, scope.flatMap(Scope.find)) match {
              case (None, _, _) =>
                Unauthorized(InvalidClientError.buildResponse)
              case (Some(client), _, _) if !client.authorizedGrantTypes.map(_.grantType).contains("client_credentials") =>
                Unauthorized(InvalidGrantError.buildResponse)
              case (_, Some(invalidScope), None) =>
                BadRequest(InvalidRequestError("invalid scope: " + invalidScope).buildResponse)
              case (Some(client), Some(_), Some(validatedScope)) =>
                accessTokenResult(Token.issue(validatedScope), validatedScope)
              case (Some(client), None, _) =>
                accessTokenResult(Token.issue(Scope.default), Scope.default)
            }
        }
      }
    )
  }

  def accessTokenResult(token: Token, requestedScope: Scope) = {
    Ok(token.buildResponse(requestedScope)).withHeaders(
      "Cache-Control" -> "no-store",
      "Pragma" -> "no-cache"
    )
  }
  
}
