package controllers

import play.api.mvc._
import play.api.data.Form
import play.api.data.Forms._
import models.AuthorizationRequest
import play.api.libs.ws.WS
import org.apache.commons.codec.binary.Base64
import play.api.libs.json.Json

/**
 * The OAuth2 client
 * - redirects user to the authorization server (`begin` action)
 * - receives access tokens (`authorizedToken` action) or authorization codes (`authorizedCode` action) from the authorization server
 */
object ClientController extends Controller {

  // Assume that the client id/secret is already registered to the authorization server
  val clientId = "test"
  val clientSecret = "pass"

  // Callback URIs
  val redirectionEndpointForCode = "http://localhost:9000/client/authorizedCode"
  val redirectionEndpointForToken = "http://localhost:9000/client/authorizedToken"

  // Authorization server's token endpoint
  val tokenEndpoint = "http://localhost:9000/token"

  def begin(responseType: String) = Action {
    val form = Form(
      mapping(
        "response_type" -> text,
        "client_id" -> optional(text),
        "redirect_uri" -> optional(text),
        "scope" -> optional(text),
        "state" -> optional(text)
      )(AuthorizationRequest.apply)(AuthorizationRequest.unapply)
    )
    val filledForm = form.fill(AuthorizationRequest(
      responseType = responseType,
      clientId = Some(clientId),
      redirectURI = Some(if (responseType == "code") redirectionEndpointForCode else redirectionEndpointForToken),
      scope = Some("default"),
      status = Some("myState")
    ))
    Ok(views.html.client.begin(filledForm))
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
  def authorizedCode(code: String, state: Option[String]) = Action {
    /**
     * 4.1.3 Access Token Request
     * http://tools.ietf.org/html/draft-ietf-oauth-v2-31#section-4.1.3
     */
    val params = Map(
      "grant_type" -> Seq("authorization_code"),
      "code" -> Seq(code),
      "redirect_uri" -> Seq(redirectionEndpointForCode),
      "client_id" -> Seq(clientId)
    )
    val response = WS.url(tokenEndpoint).withHeaders(
      "Authorization" -> ("Basic " + Base64.encodeBase64String((clientId + ":" + clientSecret).getBytes("utf-8")))
    ).post(params).await.get
    if (response.status == 200) {
      val responseBody = response.body
      val json = Json.parse(responseBody)
      val accessToken = (json \\ "access_token").head.as[String]
      Ok(views.html.client.authorized(accessToken))
    } else {
      InternalServerError("status: "+ response.status + ", body=" + response.body)
    }
  }

  def accessProtectedResource = Action { implicit request =>
    Async {
      val accessToken = request.body.asFormUrlEncoded.get("access_token").head
      WS.url("http://localhost:9000/resources/get").withHeaders("Authorization" -> ("Bearer " + accessToken)).get.map { response =>
        Ok("accessToken=" + accessToken + ", status=" + response.status + ", body=" + response.body)
      }
    }
  }

}
