package com.github.mumoshu.mumoauth.oauth2

import data._

package object roles {

  trait Client extends AuthorizationRequester
    with AccessTokenRequester
    with ProtectedResourceRequester
    with AuthorizationRequestServerComponent
    with AccessTokenRequestServerComponent
    with ProtectedResourceRequestServerComponent

  trait AuthorizationServer {
    self: AuthorizationRequestServer with AccessTokenRequestServer =>
  }

  trait ResourceOwner {
    self: AuthorizationRequestServer =>
  }

  trait ResourceServer {
    self: ProtectedResourceRequestServer =>
  }

  trait ClientComponent {
    val client: Client
  }

  trait AuthorizationServerComponent {
    val server: AuthorizationServer
  }

  trait ResourceServerComponent {
    val resourceServer: ResourceServer
  }

  trait ResourceOwnerComponent {
    val resourceOwner: ResourceOwner
  }

  /**
   * (A)  The client requests authorization from the resource owner. The
   * authorization request can be made directly to the resource owner
   * (as shown), or preferably indirectly via the authorization
   * server as an intermediary.
   * @return
   */
  trait AuthorizationRequester {
    self: AuthorizationRequestServerComponent =>

    def execute(request: AuthorizationGrantRequest): Either[Failure, AuthorizationGrant] = {
      authorizationRequestServer.serve(request)
    }
  }

  trait AuthorizationRequestServerComponent {
    val authorizationRequestServer: AuthorizationRequestServer
  }

  trait AuthorizationRequestServer {
    def serve(authorizationRequest: AuthorizationGrantRequest): Either[Failure, AuthorizationGrant]
  }

  /**
   * (B)  The client receives an authorization grant, which is a
   * credential representing the resource owner's authorization,
   * expressed using one of four grant types defined in this
   * specification or using an extension grant type.  The
   * authorization grant type depends on the method used by the
   * client to request authorization and the types supported by the
   * authorization server.
   */
  trait AccessTokenRequester {
    self: AccessTokenRequestServerComponent =>

    def execute(request: AccessTokenRequest with AuthorizationGrant): Either[Failure, AccessToken] = {
      accessTokenRequestServer.serve(request)
    }
  }

  trait AccessTokenRequestServerComponent {
    val accessTokenRequestServer: AccessTokenRequestServer
  }

  trait AccessTokenRequestServer {
    self: AuthorizationGrantValidatorComponent with AccessTokenIssuerComponent =>

    def serve(request: AccessTokenRequest with AuthorizationGrant): Either[Failure, AccessToken] = {
      for {
        authorizedRequest <- authorizationGrantValidator.validate(request).right
        accessToken <- accessTokenIssuer.issueAccessToken(authorizedRequest).right
      } yield accessToken
    }
  }

  trait AuthorizationGrantValidatorComponent {
    val authorizationGrantValidator: AuthorizationGrantValidator
  }

  trait AccessTokenIssuerComponent {
    val accessTokenIssuer: AccessTokenIssuer
  }

  trait AuthorizationGrantValidator {
    def validate[A <: AuthorizationGrant](authorizationGrant: A): Either[Failure, A with Authorized]
  }

  trait AccessTokenIssuer {
    def issueAccessToken(request: AccessTokenRequest with AuthorizationGrant with Authorized): Either[Failure, AccessToken]
  }

  trait ProtectedResourceRequester {
    self: ProtectedResourceRequestServerComponent =>

    def execute(request: ProtectedResourceRequest): Either[Failure, ProtectedResource] = {
      protectedResourceRequestServer.serve(request)
    }
  }

  trait ProtectedResourceRequestServerComponent {
    val protectedResourceRequestServer: ProtectedResourceRequestServer
  }

  trait ResourceRequestServerComponent {
    val resourceRequestServer: ResourceRequestServer
  }

  trait AccessTokenValidatorComponent {
    val accessTokenValidator: AccessTokenValidator
  }

  trait ResourceRequestServer {
    def serve(request: ProtectedResourceRequest with AccessTokenValidated): Either[Failure, ProtectedResource]
  }

  trait AccessTokenValidator {
    def validate[A <: ProtectedResourceRequest](accessToken: A): Either[Failure, A with AccessTokenValidated]
  }

  trait ProtectedResourceRequestServer {
    self: AccessTokenValidatorComponent with ResourceRequestServerComponent =>

    def serve(request: ProtectedResourceRequest): Either[Failure, ProtectedResource] = {
      for {
        validatedRequest <- accessTokenValidator.validate(request).right
        resource <- resourceRequestServer.serve(validatedRequest).right
      } yield resource
    }
  }

}
