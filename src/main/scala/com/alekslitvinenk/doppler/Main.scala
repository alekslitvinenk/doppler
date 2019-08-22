package com.alekslitvinenk.doppler

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
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

  val route =
    extractHost { host =>
      getFromResourceDirectory(host)
    }

  Http().bindAndHandle(sqlShingle(logbackShingle(route)), interface, port)
}
