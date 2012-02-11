package com.github.mumoshu.oauth

import com.github.mumoshu.oauth.credentials.TemporaryCredentials

/**
 * 2-Legged, 3-Legged, n-Legged
 *
 * The number of legs used to describe an OAuth request typically refers to the number of parties
 * involved. In the simple OAuth flow: a client, a server, and a resource owner, the flow is
 * described as 3-legged. When the client is also the resource owner (that is, acting on behalf of
 * itself), it is described as 2-legged. Additional legs usually mean different things to different
 * people, but in general mean that access is shared by the client with other clients
 * (re-delegation).
 */
object oauth {

  def signatureBaseString: String = ""
}

/**
 * In OAuth 1.0, the secret half of each set of credentials is defined as a symmetric shared
 * secret. This means that both the client and server must have access to the same secret string.
 * However, OAuth supports an RSA-based authentication method which uses an asymmetric client
 * secret. The different credentials are explained in more detailed later on.
 */
object secrets {
}

trait HashAlgorithm {
  def digest(data: String): Unit
}
object SHA1 extends HashAlgorithm {
  def digest(data: String) = {}
}

trait SharedSecret {
  type ServerSide
  type ClientSide
}

object Combination extends SharedSecret {
  type ServerSide = CombinationOfClientSecretAndTokenSecret
  type ClientSide = CombinationOfClientSecretAndTokenSecret
}
case class CombinationOfClientSecretAndTokenSecret(clientSecret: String, tokenSecret: String) extends SharedSecret

sealed trait Method
object Get extends Method
object Post extends Method

case class FakeRequest(
  method: String,
  path: String,
  queryString: Option[String] = None,
  httpVersion: String = "1.1",
  host: String,
  authorization: Option[String] = None,
  contentType: Option[String] = None,
  messageBody: Option[String] = None,
  port: Int = 80,
  scheme: String = "http") extends Request

trait SignedRequest

trait PublicKey {
  def verify(r: SignedRequest): Request
}

// Used by RSA-SHA-1 to sign a request.
trait PrivateKey {
  def sign(r: Request): SignedRequest
}

object AsymmetricSharedSecret extends SharedSecret {
  type ServerSide = PublicKey
  type ClientSide = PrivateKey
}

/**
 * OAuth defines 3 signature methods used to sign and verify requests
 */
trait SignatureMethod

// Client signs a request by RSA-SHA-1
object RSASHA1 extends SignatureMethod {
  def digest(data: String, secret: PrivateKey) = ""
}

object PLAINTEXT extends SignatureMethod {
  def digest(data: String, secret: CombinationOfClientSecretAndTokenSecret) = ""
}

object HMACSHA1 extends SignatureMethod {
  def digest(data: String, secret: CombinationOfClientSecretAndTokenSecret) = ""
}

/**
 * Credentials and Tokens
 *
 * OAuth uses three kinds of credentials: client credentials, temporary credentials, and token
 * credentials. The original version of the specification used a different set of terms for these
 * credentials: consumer key and secret (client credentials), request token and secret (temporary
 * credentials), and access token and secret (token credentials). The specification still uses a
 * parameter name ‘oauth_consumer_key‘ for backwards compatibility.
 */
object credentials {

  // Former consumer key and secret

  /**
   * The client credentials are used to authenticate the client. This allows the server to collect
   * information about the clients using its services, offer some clients special treatment such
   * as throttling-free access, or provide the resource owner with more information about the
   * clients seeking to access its protected resources. In some cases, the client credentials
   * cannot be trusted and can only be used for informational purposes only, such as in desktop
   * application clients.
   */
  case class ClientCredentials

  // Former request token and secret

  /**
   * The OAuth authorization process also uses a set of temporary credentials which are used to
   *  identify the authorization request. In order to accommodate different kind of clients
   *  (web-based, desktop, mobile, etc.), the temporary credentials offer additional flexibility
   *  and security.
   */
  case class TemporaryCredentials

  case class AuthorizedTemporaryCredentials

  case class AuthorizedRequestToken

  // Former access token and secret

  /**
   * Token credentials are used in place of the resource owner’s username and password. Instead
   * of having the resource owner share its credentials with the client, it authorizes the server
   * to issue a special class of credentials to the client which represent the access grant given
   * to the client by the resource owner. The client uses the token credentials to access the
   * protected resource without having to know the resource owner’s password.
   *
   * Token credentials include a token identifier, usually (but not always) a random string of
   * letters and numbers that is unique, hard to guess, and paired with a secret to protect the
   * token from being used by unauthorized parties. Token credentials are usually limited in scope
   * and duration, and can be revoked at any time by the resource owner without affecting other
   * token credentials issued to other clients.
   */
  case class TokenCredentials(
    scope: Option[String],
    duration: Option[Int])

  case class AccessToken()

}

/**
 * OAuth is a simple way to publish and interact with protected data. It's also a safer and more
 * secure way for people to give you access. We've kept it simple to save you time.
 */
class Consumer {
}

object ThreeLegged {

}

trait Client {
  def obtainTemporaryCredentials: Unit
  def obtainTokenCredentials: Unit
  def requestResourceOwnerAuthorizaton: Unit

  def requestTemporaryCredentials: Unit
  def redirectsResourceOwner: Unit
}

/**
 * If you're storing protected data on your users' behalf, they shouldn't be spreading their
 * passwords around the web to get access to it. Use OAuth to give your users access to their
 * data while protecting their account credentials.
 */
trait Provider {

}

trait Server {
  def markResourceOwnerAuthorized(temporaryCredentials: TemporaryCredentials): Unit
}

trait ResourceOwner {
  def approvesRequest(): Unit
  def revokeAccessToken(): Unit
}
