package oauth2.service

import oauth2._
import oauth2.definition.ScopeDefinition
import oauth2.entity.Client
import oauth2.error._
import oauth2.value_object._

class AuthorizationService(clientSvc: ClientService, tokenSvc: TokenService, scopeDef: ScopeDefinition, defaultScope: Option[Scope]) {
  def validateCode(clientId: String, redirectURI: Option[String], requestedScope: Option[String], state: Option[String]): Either[TokenError, CodeGrantRequest] = {
    (clientSvc.find(clientId), redirectURI, requestedScope, requestedScope.flatMap(scopeDef.find)) match {
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

        (scope, validScope, defaultScope) match {
          case (Some(a), None, None) =>
            Left(InvalidScopeError("Undefined scope: " +  a, redirectURI))
          case (_, a, b) =>
            // "If the client omits the scope parameter when requesting
            // authorization, the authorization server MUST either process the
            // request using a pre-defined default value, or fail the request
            // indicating an invalid scope."
            // @see http://tools.ietf.org/html/draft-ietf-oauth-v2-31#section-3.3
            a.orElse(b)
              .map { x => Right(CodeGrantRequest(client, redirectionURI.get, x, state)) }
              .getOrElse { Left(InvalidScopeError("Missing scope", redirectURI)) }
        }
    }
  }

  // http://tools.ietf.org/html/draft-ietf-oauth-v2-31#section-4.2.1
  def validateImplicit(clientId: String, redirectURI: Option[String], requestedScope: Option[String], state: Option[String]): Either[TokenError, ImplicitGrantRequest] = {
    (clientSvc.find(clientId), redirectURI, requestedScope, requestedScope.flatMap(scopeDef.find)) match {
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
        (scope, validScope, defaultScope) match {
          case (Some(a), None, None) =>
            Left(InvalidScopeError("Undefined scope: " +  a, redirectURI))
          case (_, a, b) =>
            // "If the client omits the scope parameter when requesting
            // authorization, the authorization server MUST either process the
            // request using a pre-defined default value, or fail the request
            // indicating an invalid scope."
            // @see http://tools.ietf.org/html/draft-ietf-oauth-v2-31#section-3.3
            a.orElse(b)
              .map { x => Right(ImplicitGrantRequest(client, redirectionURI.get, x, state)) }
              .getOrElse { Left(InvalidScopeError("Missing scope", redirectURI)) }
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
    (clientSvc.find(clientId), redirectURI, scopeDef.find(requestedScope), scopeDef.find(authorizedScope)) match {
      case (None, _, _, _) =>
        Left(server.ClientNotFoundError)
      case (_, _, None, _) =>
        Left(server.InvalidRequestError("Undefined scope: " + requestedScope))
      case (_, _, _, None) =>
        Left(server.InvalidRequestError("Undefined scope: " + authorizedScope))
      case (Some(client), redirectionURI, Some(requestedScope), Some(authorizedScope)) =>
        val token = tokenSvc.issue(authorizedScope)
        Right(redirectionURI + "#" + tokenSvc.toURIComponentBuilder(token).buildURIComponent(requestedScope, state))
    }
  }
}
