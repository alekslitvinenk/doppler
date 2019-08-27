package com.alekslitvinenk.doppler

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.stream.ActorMaterializer
import com.alekslitvinenk.logshingles.dsl.ShinglesDirectives._

import scala.concurrent.ExecutionContext
import scala.util.Try

object Main extends App {

  implicit val system: ActorSystem = ActorSystem("my-system")
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  implicit val executionContext: ExecutionContext = system.dispatcher


  private val interface = Try(args(0)).getOrElse("127.0.0.1")
  private val port = Try(args(1).toInt).getOrElse(8080)

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
      } ~ {
        redirect("/404/", StatusCodes.TemporaryRedirect)
      }
    }

  private val shingledRoute = sqlShingle(logbackShingle(route))

  Http().bindAndHandle(shingledRoute, interface, port)
}
