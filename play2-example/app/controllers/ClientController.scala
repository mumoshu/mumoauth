package controllers

import play.api.mvc._
import play.api.data.Form
import play.api.data.Forms._
import models.AuthorizationRequest
import play.api.libs.ws.WS
import org.apache.commons.codec.binary.Base64
import play.api.libs.json.Json
import com.codahale.jerkson.ParsingException
import play.api.Logger
import play.core.parsers.FormUrlEncodedParser

case class OAuth2Settings(
  clientId: String,
  clientSecret: String,
  authorizationEndpoint: String,
  tokenEndpoint: String,
  requiresClientSecretInRequestParameter: Boolean = false
)

/**
 * The OAuth2 client
 * - redirects user to the authorization server (`begin` action)
 * - receives access tokens (`authorizedToken` action) or authorization codes (`authorizedCode` action) from the authorization server
 */
object ClientController extends Controller {

  val oauth2Services = Map(
    "local" -> OAuth2Settings(
      // Assume that the client id/secret is already registered to the authorization server
      clientId = "test",
      clientSecret = "pass",
      authorizationEndpoint = "http://localhost:9000/authorize",
      // Authorization server's token endpoint
      tokenEndpoint = "http://localhost:9000/token"
    ),
    "github" -> OAuth2Settings(
      clientId = "PUT YOUR CLIENT ID HERE",
      clientSecret = "PUT YOUR CLIENT SECRET HERE",
      authorizationEndpoint = "https://github.com/login/oauth/authorize",
      tokenEndpoint = "https://github.com/login/oauth/access_token",
      requiresClientSecretInRequestParameter = true
    )
  )

  val defaultScopes = Map(
    "local" -> "default",
    "github" -> "notifications"
  )

  val exampleResourceUrls = Map(
    "local" -> "http://localhost:9000/resources/get",
    "github" -> "https://api.github.com/notifications"
  )

  // Callback URIs
  val redirectionEndpointForCode = "http://localhost:9000/client/authorizedCode"
  val redirectionEndpointForToken = "http://localhost:9000/client/authorizedToken"

  def begin(responseType: String, service: String) = Action {
    val form = Form(
      mapping(
        "response_type" -> text,
        "client_id" -> optional(text),
        "redirect_uri" -> optional(text),
        "scope" -> optional(text),
        "state" -> optional(text)
      )(AuthorizationRequest.apply)(AuthorizationRequest.unapply)
    )
    val oauth2Settings = oauth2Services(service)
    val baseRedirectUrl = if (responseType == "code") redirectionEndpointForCode else redirectionEndpointForToken
    val redirectUrl = baseRedirectUrl + "?service=" + service
    val filledForm = form.fill(AuthorizationRequest(
      responseType = responseType,
      clientId = Some(oauth2Settings.clientId),
      redirectURI = Some(redirectUrl),
      scope = Some(defaultScopes(service)),
      status = Some("myState")
    ))
    Ok(views.html.client.begin(filledForm, service, Call("GET", oauth2Settings.authorizationEndpoint)))
  }

  def authorizedToken = Action {
    Ok(views.html.client.authorizedToken())
  }

  /**
   * 4.1.2. Authorization Response
   * http://tools.ietf.org/html/draft-ietf-oauth-v2-31#section-4.1.2
   * @param code authorization code generated by the authorization server
   * @param state the exact value the authorization server received from the client
   * @return
   */
  def authorizedCode(code: String, state: Option[String], service: String) = Action {
    val oauth2Settings = oauth2Services(service)
    /**
     * 4.1.3 Access Token Request
     * http://tools.ietf.org/html/draft-ietf-oauth-v2-31#section-4.1.3
     */
    val params = Map(
      "grant_type" -> Seq("authorization_code"),
      "code" -> Seq(code),
      "redirect_uri" -> Seq(redirectionEndpointForCode),
      "client_id" -> Seq(oauth2Settings.clientId)
    ) ++ {
      if (oauth2Settings.requiresClientSecretInRequestParameter) Map("client_secret" -> Seq(oauth2Settings.clientSecret)) else Map.empty
    }
    val basicAuth: String = "Basic " + Base64.encodeBase64String((oauth2Settings.clientId + ":" + oauth2Settings.clientSecret).getBytes("utf-8"))
    val response = WS.url(oauth2Settings.tokenEndpoint).withHeaders(
      "Authorization" -> basicAuth
    ).post(params).await.get
    Logger.info("Basic auth= " + basicAuth)
    Logger.info("response headers= " + response.ahcResponse.getHeaders)
    if (response.status == 200) {
      response.ahcResponse.getContentType.split(";").map(_.stripSuffix(" ")) match {
        case Array("application/x-www-form-urlencoded", _) =>
          val responseParameters = FormUrlEncodedParser.parse(response.body)
          try {
            val tokenType = responseParameters("token_type").head
            val accessToken = responseParameters("access_token").head
            Ok(views.html.client.authorized(accessToken, service))
          } catch {
            case e: NoSuchElementException =>
              Logger.error("Missing parameter(s) in response: " + responseParameters)
              ServiceUnavailable("The external OAuth2 service '" + service + "' is going wrong. Try again later.")
          }
        case _ =>
          val responseBody = response.body
          try {
            val json = Json.parse(responseBody)
            val accessToken = (json \\ "access_token").head.as[String]
            Ok(views.html.client.authorized(accessToken, service))
          } catch {
            case e: ParsingException =>
              Logger.error("Unexpected response from the server: Content-type=" + response.ahcResponse.getContentType + ", body=" + responseBody + ", params=" + params, e)
              ServiceUnavailable("The external OAuth2 service '" + service + "' is going wrong. Try again later.")
          }
      }
    } else {
      InternalServerError("status: "+ response.status + ", body=" + response.body)
    }
  }

  def accessProtectedResource = Action { implicit request =>
    Async {
      val parameters = request.body.asFormUrlEncoded.get
      val accessToken = parameters("access_token").head
      val service = parameters("service").head
      WS.url(exampleResourceUrls(service)).withHeaders("Authorization" -> ("Bearer " + accessToken)).get.map { response =>
        Ok("accessToken=" + accessToken + ", status=" + response.status + ", body=" + response.body)
      }
    }
  }

}