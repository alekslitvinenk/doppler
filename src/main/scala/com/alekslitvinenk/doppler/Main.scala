package com.alekslitvinenk.doppler

import akka.actor.ActorSystem
import akka.http.scaladsl.model.{StatusCodes, Uri}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.{ConnectionContext, Http, HttpsConnectionContext}
import akka.stream.ActorMaterializer
import com.alekslitvinenk.logshingles.dsl.ShinglesDirectives._
import com.typesafe.sslconfig.akka.AkkaSSLConfig
import com.typesafe.sslconfig.akka.util.AkkaLoggerFactory
import com.typesafe.sslconfig.ssl.ConfigSSLContextBuilder

import scala.concurrent.ExecutionContext

object Main extends App {

  implicit val system: ActorSystem = ActorSystem("my-system")
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  implicit val executionContext: ExecutionContext = system.dispatcher

  private val interface = sys.env.getOrElse("BIND_INTERFACE", "0.0.0.0")

  private val httpsRedirectRoute: Route = extractUri(redirectHttps)
  private def redirectHttps(uri: Uri): Route = redirect(toHttps(uri), StatusCodes.PermanentRedirect)
  private def toHttps(uri: Uri): Uri = uri.copy(scheme = "https")

  private val baseDir = sys.env.getOrElse("WWW_DIR", "/var/www/hosts")
  private val hostsDir = s"$baseDir/hosts"
  private val enableSSL: Boolean = sys.env.getOrElse("ENABLE_SSL", "false").toBoolean
  private val redirectToHTTPS: Boolean = sys.env.getOrElse("REDIRECT_TO_HTTPS", "false").toBoolean

  private val route =
    extractHost { host =>
      val hostParts = host.split('.')
      val redirectHost = if (hostParts.length == 3 && hostParts(0) == "www")
        hostParts.drop(1).mkString(".")
      else
        host

      pathSingleSlash {
        getFromFile(s"$hostsDir/$redirectHost/index.html")
      } ~ path(Segments . /) { paths =>
        val redirectUrl = paths.mkString("/") + "/index.html"
        getFromFile(s"$hostsDir/$redirectHost/$redirectUrl")
      } ~ {
        getFromDirectory(s"$hostsDir/$redirectHost")
      }
    }
  
  private val baseRoot = if (enableSSL && redirectToHTTPS) httpsRedirectRoute else route
  private val shingledRoute = sqlShingle(logbackShingle(baseRoot))
  
  Http().bindAndHandle(baseRoot, interface, 8080)
  
  if (enableSSL) {
    val sslConfig = AkkaSSLConfig.get(system)
    val keyManagerFactory = sslConfig.buildKeyManagerFactory(sslConfig.config)
    val trustManagerFactory = sslConfig.buildTrustManagerFactory(sslConfig.config)
    val ctx = new ConfigSSLContextBuilder(new AkkaLoggerFactory(system), sslConfig.config, keyManagerFactory, trustManagerFactory).build()
    val https: HttpsConnectionContext = ConnectionContext.https(ctx)
    val shingledHttpsRoute = sqlShingle(logbackShingle(route))
    Http().bindAndHandle(shingledHttpsRoute, interface, 9443, https)
  }
}
