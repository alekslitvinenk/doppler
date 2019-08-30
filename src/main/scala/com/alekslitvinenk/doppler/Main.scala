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
import scala.util.Try

object Main extends App {

  implicit val system: ActorSystem = ActorSystem("my-system")
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  implicit val executionContext: ExecutionContext = system.dispatcher

  private val interface = Try(args(0)).getOrElse("127.0.0.1")

  private val httpsRedirectRoute: Route = extractUri(redirectHttps)
  private def redirectHttps(uri: Uri): Route = redirect(toHttps(uri), StatusCodes.PermanentRedirect)
  private def toHttps(uri: Uri): Uri = uri.copy(scheme = "https")

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

  private val shingledRoute = logbackShingle(route)

  val sslConfig = AkkaSSLConfig.get(system)

  val keyManagerFactory = sslConfig.buildKeyManagerFactory(sslConfig.config)
  val trustManagerFactory = sslConfig.buildTrustManagerFactory(sslConfig.config)
  val ctx = new ConfigSSLContextBuilder(new AkkaLoggerFactory(system), sslConfig.config, keyManagerFactory, trustManagerFactory).build()

  val https: HttpsConnectionContext = ConnectionContext.https(ctx)

  Http().bindAndHandle(httpsRedirectRoute, interface, 8080)
  Http().bindAndHandle(shingledRoute, interface, 9443, https)
}
