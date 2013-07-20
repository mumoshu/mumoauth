package oauth2.entity

import oauth2.value_object.Scope

/**
  * Authorization code confirmed by user
  * @param code the authorization code
  * @param requestedScope the scope of this authorization code the client requested
  * @param authorizedScope the actual scope of this authorization code confirmed by user. this may differ from requestedScope.
  * @param redirectURI redirect_uri provided on the request
  */
case class Code(code: String, requestedScope: Scope, authorizedScope: Scope, redirectURI: Option[String] = None)
