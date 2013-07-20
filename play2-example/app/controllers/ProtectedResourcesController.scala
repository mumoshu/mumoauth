package controllers

import play.api.mvc._
import oauth2.{Token, AuthorizationHeader}
import models.TokenSvc

object ProtectedResourcesController extends Controller {

  def get = Action { implicit request =>
    // 7. Accessing Protected Resources
    // http://tools.ietf.org/html/draft-ietf-oauth-v2-31#section-7
    request.headers.get(AuthorizationHeader.Name).map(_.split(" ")) match {
      case Some(Array("Bearer", accessToken)) =>
        TokenSvc.find(accessToken) match {
          case Some(token) if !token.isExpired =>
            Ok("OK")
          case _ =>
            Unauthorized
        }
      case _ =>
        BadRequest
    }
  }

}
