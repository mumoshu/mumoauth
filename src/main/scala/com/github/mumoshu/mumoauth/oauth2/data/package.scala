package com.github.mumoshu.mumoauth.oauth2

package object data {
  trait AuthorizationGrantRequest
  trait AuthorizationGrant
  trait Authorized {
    self: AuthorizationGrant =>
  }
  trait AccessTokenRequest
  trait AccessToken
  trait ProtectedResourceRequest {
    val accessToken: AccessToken
  }
  trait AccessTokenValidated {
    self: ProtectedResourceRequest =>
  }
  trait ProtectedResource

  trait Failure
}
