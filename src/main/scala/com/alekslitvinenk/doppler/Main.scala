package com.alekslitvinenk.doppler

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.stream.ActorMaterializer
import com.alekslitvinenk.doppler.directive._
import com.alekslitvinenk.doppler.util.Utils.getHostToRouteMap

import scala.collection.concurrent.TrieMap
import scala.concurrent.ExecutionContext

object Main extends App {

  implicit val system: ActorSystem = ActorSystem("my-system")
  implicit val executionContext: ExecutionContext = system.dispatcher
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  
  private val log = system.log
  
  private val interface = sys.env.getOrElse("BIND_INTERFACE", "0.0.0.0")
  private val baseDir = sys.env.getOrElse("WWW_DIR", "/var/www/hosts")
  //private val baseDir = sys.env.getOrElse("WWW_DIR", "/Users/aleksandrlitvinenko/dummy-hosts")
  
  private val hostsDir = s"$baseDir/hosts"
  
  private val hostsMap: TrieMap[String, Route] = getHostToRouteMap(hostsDir)
    
  private val route =
    redirectToNoWwwHost {
      redirectToNoTrailingSlash {
        resolveHostRoute(hostsMap)
      }
    }
  
  private val bindingFuture = Http().bindAndHandle(route, interface, 8080)
  
  sys.addShutdownHook {
    bindingFuture
      .flatMap(_.unbind()) // trigger unbinding from the port
      .onComplete { _ =>
        log.info("Unbinding complete. shutting down akka system")
        system.terminate() // and shutdown when done
      }
  }
}
