package com.alekslitvinenk.doppler

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives._
import akka.stream.ActorMaterializer

import scala.concurrent.ExecutionContext
import scala.util.Try

object Main extends App {

  implicit val system: ActorSystem = ActorSystem("my-system")
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  implicit val executionContext: ExecutionContext = system.dispatcher

  private val interface = Try(args(0)).getOrElse("127.0.0.1")

  // TODO: Enable when issues/12 is done
  /*private val httpsRedirectRoute: Route = extractUri(redirectHttps)
  private def redirectHttps(uri: Uri): Route = redirect(toHttps(uri), StatusCodes.PermanentRedirect)
  private def toHttps(uri: Uri): Uri = uri.copy(scheme = "https")*/

  private val baseDir = sys.env.getOrElse("WWW_DIR", "/var/www/hosts")
  private val hostsDir = s"$baseDir/hosts"

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

  // TODO: Enable when issues/12 is done
  /*val sslConfig = AkkaSSLConfig.get(system)

  val keyManagerFactory = sslConfig.buildKeyManagerFactory(sslConfig.config)
  val trustManagerFactory = sslConfig.buildTrustManagerFactory(sslConfig.config)
  val ctx = new ConfigSSLContextBuilder(new AkkaLoggerFactory(system), sslConfig.config, keyManagerFactory, trustManagerFactory).build()

  val https: HttpsConnectionContext = ConnectionContext.https(ctx)*/

  Http().bindAndHandle(route, interface, 8080)
  //Http().bindAndHandle(route, interface, 9443, https)
}
