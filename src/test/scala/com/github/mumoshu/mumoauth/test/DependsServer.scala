package com.github.mumoshu.mumoauth.test

import org.specs2.specification.Scope
import com.github.mumoshu.mumoauth.server._
import com.github.mumoshu.mumoauth.{ TokenCredential, TemporaryCredential, HMACSHA1 }
import com.github.mumoshu.mumoauth.HMACSHA1
import com.github.mumoshu.mumoauth.TokenCredential
import com.github.mumoshu.mumoauth.TemporaryCredential
import scala.Some

trait DependsServer extends Scope {

  // oauth_consumer_key
  val clientIdentifier = "dpf43f3p2l4k3l03"
  val clientSharedSecret = "kd94hf93k423kf44"

  val temporaryIdentifier = "hh5s93j4hdidpola"
  val temporarySharedSecret = "hdhd0244k9j7ao03"

  val tokenIdentifier = "nnch734d00sl2jdk"
  val tokenSharedSecret = "pfkkdhi9sl3r4s00"

  val printerWebSite = "printer.example.net"
  val photosWebSite = "photos.example.net"

  val signatureMethod = "HMAC-SHA1"

  val temporaryCredentialRequestURI = "https://photos.example.net/initiate"
  val resourceOwnerAuthorizationURI = "https://photos.example.net/authorize"
  val tokenRequestURI = "https://photos.example.net"

  val testServerConfiguration = new ServerConfiguration {
    def signatureMethod(clientSecret: String, tokenSecret: String) = HMACSHA1(
      clientSecret = clientSecret,
      tokenSecret = tokenSecret
    )

    val clientCredentialsStore = new ClientCredentialsStore {
      def getSecretByToken(token: String) =
        if (token == clientIdentifier)
          Some(clientSharedSecret)
        else
          None
    }

    val temporaryCredentialsStore = new TemporaryCredentialsStore {
      def generate() = TemporaryCredential(temporaryIdentifier, temporarySharedSecret)

      def getSecretByIdentifier(identifier: String) = {
        if (identifier == temporaryIdentifier)
          Some(temporarySharedSecret)
        else
          None
      }
    }

    val tokenCredentialsStore = new TokenCredentialsStore {
      def generate() = TokenCredential(tokenIdentifier, tokenSharedSecret)

      def getSecretByIdentifier(identifier: String) = {
        if (identifier == tokenIdentifier)
          Some(tokenSharedSecret)
        else
          None
      }
    }
  }

  trait TestServerConfigurationComponent extends ServerConfigurationComponent {
    def serverConfiguration = testServerConfiguration
  }

  val server = new Server with TestServerConfigurationComponent {
    //    val tokenCredentialsStore = FakeTokenCredentialsStore.empty
    //    val temporaryCredentialsStore = FakeTemporaryCredentialsStore.empty
    //    val userSessionsStore = FakeUserSessionsStore()

    val initiatePath = "/initiate"
    val resourceOwnerAuthorizationPath = "/authorize"
    val tokenRequestPath = "/token"

    val responseGenerator = FakeResponseGenerator

  }

}
