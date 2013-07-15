package com.github.mumoshu.mumoauth.server

import com.github.mumoshu.mumoauth.credentials.TemporaryCredentials
import com.github.mumoshu.mumoauth._

trait ResponseGenerator {
  def ok(contentType: String, body: String): Response

  def redirect(url: String, setCookie: Option[String]): Response
}

// The OAuth client must implement this to communicate with the server.
trait RequestGenerator {

  def get(scheme: String, path: String, queryString: Option[String], host: String, port: Option[Int]): Request

  def post(scheme: String, path: String, host: String, port: Option[Int], contentType: Option[String], body: Option[String], authorization: Option[String]): Request
}

trait ClientCredentialsStore {

  def getSecretByToken(token: String): Option[String]
}

trait TemporaryCredentialsStore {

  def generate(): TemporaryCredential

  def getSecretByIdentifier(identifier: String): Option[String]
}

trait TokenCredentialsStore {

  def generate(): TokenCredential

  def getSecretByIdentifier(identifier: String): Option[String]
}

/**
 * Each OAuth 2.0 server should be configured with its specitic:
 * <ul>
 *   <li>scopes</li>
 *   <li>clients</li>
 *   <li>temporary tokens</li>
 *   <li>tokens</li>
 *   <li>signature method</li>
 * </ul>
 */
trait ServerConfiguration {
  // val availableScopes: List[Scope]
  val clientCredentialsStore: ClientCredentialsStore

  val temporaryCredentialsStore: TemporaryCredentialsStore

  val tokenCredentialsStore: TokenCredentialsStore

  // TODO Create SignatureMethodProvider and facade this to that
  def signatureMethod(clientSecret: String, tokenSecret: String): SignatureMethod
}

trait ServerConfigurationComponent {
  def serverConfiguration: ServerConfiguration
}

trait Server {
  self: ServerConfigurationComponent =>

  // Incorporate all configuration parameters to this server
  def clientCredentialsStore = serverConfiguration.clientCredentialsStore
  def temporaryCredentialsStore = serverConfiguration.temporaryCredentialsStore
  def tokenCredentialsStore = serverConfiguration.tokenCredentialsStore
  def signatureMethod(clientSecret: String, tokenSecret: String) = serverConfiguration.signatureMethod(clientSecret, tokenSecret)

  //def markResourceOwnerAuthorized(temporaryCredentials: TemporaryCredentials): Unit

  //
  // Paths
  //

  /** Temporary Credential Request */
  val initiatePath: String

  /** Resource Owner Authorization */
  val resourceOwnerAuthorizationPath: String

  /** Token Request */
  val tokenRequestPath: String

  //
  // Generators
  //

  val responseGenerator: ResponseGenerator

  //
  // Credential stores
  //

  def receive(r: Request): Response = {
    r.path match {
      case s if s == initiatePath => initiate(r)
      case s if s == tokenRequestPath => tokenRequest(r)
      case s => throw new RuntimeException("The action for the request path '%s' is not implemented yet.".format(s))
    }
  }

  private def clientSecretOf(r: Request): Option[String] = {
    r.oauthClientIdentifier match {
      case Some(identifier) =>
        clientCredentialsStore.getSecretByToken(identifier)
      case _ => throw new RuntimeException("Client identifier is not found in the Authorization header.")
    }
  }

  private def temporaryOrTokenSecretOf(r: Request): Option[String] = {
    r.oauthTemporaryOrTokenIdentifier match {
      case Some(identifier) =>
        temporaryCredentialsStore.getSecretByIdentifier(identifier)
      case _ => throw new RuntimeException("Temporary identifier is not found in the Authorization header.")
    }
  }

  /**
   * Validates an access to the Temporary Credential Request URI.
   * A request is valid when the re-calculated signature matches the 'oauth_signature' value in the Authorization header.
   * @param r
   * @return
   */
  private def validateInitiate(r: Request): Boolean = {
    val clientSecret = clientSecretOf(r) match {
      case Some(secret) => secret
      case _ => throw new RuntimeException("The client secret for identifier is not found.")
    }
    r.oauthSignature.exists(_ == signatureMethod(clientSecret, "").sign(BaseString.baseStringOf(r)))
  }

  /**
   * Validates an access to the Token Request URI.
   * @param r
   * @return
   */
  private def validateTokenRequest(r: Request): Boolean = {
    val clientSecret = clientSecretOf(r) match {
      case Some(secret) => secret
      case _ => throw new RuntimeException("The client secret for identifier is not found.")
    }
    val temporarySecret = temporaryOrTokenSecretOf(r) match {
      case Some(secret) => secret
      case _ => throw new RuntimeException("The temporary secret for the temporary identifier is not found.")
    }
    r.oauthSignature.exists(_ == signatureMethod(clientSecret, temporarySecret).sign(BaseString.baseStringOf(r)))
  }

  /**
   * Validates an access to the protected resource.
   * @param r
   * @return
   */
  def validateResourceRequest(r: Request): Boolean = {
    val clientSecret = clientSecretOf(r) match {
      case Some(secret) => secret
      case _ => throw new RuntimeException("The client secret for identifier is not found.")
    }
    val tokenSecret = temporaryOrTokenSecretOf(r) match {
      case Some(secret) => secret
      case _ => throw new RuntimeException("The token secret for the identifier is not found.")
    }
    r.oauthSignature.exists(_ == signatureMethod(clientSecret, tokenSecret).sign(BaseString.baseStringOf(r)))
  }

  def initiate(r: Request): Response = {
    if (validateInitiate(r)) {
      val TemporaryCredential(identifier, secret) = temporaryCredentialsStore.generate()
      val body = "oauth_token=%s&oauth_token_secret=%s&oauth_callback_confirmed=true".format(identifier, secret)
      responseGenerator.ok(
        contentType = "application/x-www-form-urlencoded",
        body = body
      )
    } else {
      throw new RuntimeException("Not implemented yet.");
    }
  }

  def tokenRequest(r: Request): Response = {
    if (validateTokenRequest(r)) {
      val TokenCredential(identifier, secret) = tokenCredentialsStore.generate()
      responseGenerator.ok(
        contentType = "application/x-www-form-urlencoded",
        body = "oauth_token=%s&oauth_token_secret=%s".format(identifier, secret)
      )
    } else {
      throw new RuntimeException("Not implemented yet.")
    }
  }

  def redirectWithCode(uri: String, code: String): Response = {
    responseGenerator.redirect(
      url = uri,
      setCookie = None
    )
  }

}
