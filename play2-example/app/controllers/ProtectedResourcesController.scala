package controllers

import play.api.mvc._
import models.Token

object ProtectedResourcesController extends Controller {

  def get = Action { implicit request =>
    // 7. Accessing Protected Resources
    // http://tools.ietf.org/html/draft-ietf-oauth-v2-31#section-7
    request.headers.get("Authorization").map(_.split(" ")) match {
      case Some(Array("Bearer", accessToken)) =>
        Token.find(accessToken) match {
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
