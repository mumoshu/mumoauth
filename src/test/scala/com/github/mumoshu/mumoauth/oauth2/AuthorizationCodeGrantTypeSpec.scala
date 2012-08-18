package com.github.mumoshu.mumoauth.oauth2

import org.specs2.mutable._

object AuthorizationCodeGrantTypeSpec extends Specification {

  //
  // (A) Client |-- Authorization Request -->| Resource Owner
  //
  // The client requests authorization from the resource owner.  The
  // authorization request can be made directly to the resource owner
  // (as shown), or preferably indirectly via the authorization
  // server as an intermediary.


  //
  // (B) Client |<-- Authorization Grant --| Resource Owner
  //
  // The client receives an authorization grant which is a credential
  //  representing the resource owner's authorization, expressed using
  //    one of four grant types defined in this specification or using
  //  an extension grant type.  The authorization grant type depends
  //  on the method used by the client to request authorization and
  //  the types supported by the authorization server.

  // (C) Authorization Grant is sent to Authorization Server


  // (D) Access Token is sent to Client


  // (E) Access Token is sent to Resource Server


  // (F) Client has access to Protected Resource






}
