package com.alekslitvinenk.doppler

import java.io.InputStream
import java.security.{KeyStore, SecureRandom}

import akka.actor.ActorSystem
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.{ConnectionContext, Http, HttpsConnectionContext}
import akka.stream.ActorMaterializer
import com.alekslitvinenk.logshingles.dsl.ShinglesDirectives._
import javax.net.ssl.{KeyManagerFactory, SSLContext, TrustManagerFactory}

import scala.concurrent.ExecutionContext
import scala.util.Try

object Main extends App {

  implicit val system: ActorSystem = ActorSystem("my-system")
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  implicit val executionContext: ExecutionContext = system.dispatcher

  // Manual HTTPS configuration

  val password: Array[Char] = "".toCharArray // do not store passwords in code, read them from somewhere safe!

  val ks: KeyStore = KeyStore.getInstance("PKCS12")
  val keystore: InputStream = getClass.getClassLoader.getResourceAsStream("certificates/alekslitvinenk.com/server.p12")

  require(keystore != null, "Keystore required!")
  ks.load(keystore, password)

  val keyManagerFactory: KeyManagerFactory = KeyManagerFactory.getInstance("SunX509")
  keyManagerFactory.init(ks, password)

  val tmf: TrustManagerFactory = TrustManagerFactory.getInstance("SunX509")
  tmf.init(ks)

  val sslContext: SSLContext = SSLContext.getInstance("TLS")
  sslContext.init(keyManagerFactory.getKeyManagers, tmf.getTrustManagers, new SecureRandom)
  val https: HttpsConnectionContext = ConnectionContext.https(sslContext)

  private val interface = Try(args(0)).getOrElse("127.0.0.1")

  private val route =
    extractHost { host =>
      val hostParts = host.split('.')
      val redirectHost = if (hostParts.length == 3 && hostParts(0) == "www")
        hostParts.drop(1).mkString(".")
      else
        host

      pathSingleSlash {
        getFromResource(s"hosts/$redirectHost/index.html")
      } ~ path(Segments . /) { paths =>
        val redirectUrl = paths.mkString("/") + "/index.html"
        getFromResource(s"hosts/$redirectHost/$redirectUrl")
      } ~ {
        getFromResourceDirectory(s"hosts/$redirectHost")
      }
    }

  private val shingledRoute = sqlShingle(logbackShingle(route))

  Http().bindAndHandle(shingledRoute, interface, 8080)
  Http().bindAndHandle(shingledRoute, interface, 9443, https)
}
