package com.alekslitvinenk.doppler

import com.alekslitvinenk.doppler.directive._
import com.alekslitvinenk.doppler.util.Utils.getHostToRouteMap
import org.apache.pekko.actor.ActorSystem
import org.apache.pekko.http.scaladsl.Http
import org.apache.pekko.http.scaladsl.server.Route

import scala.collection.concurrent.TrieMap
import scala.concurrent.ExecutionContext

object Main extends App {

  implicit val system: ActorSystem = ActorSystem("my-system")
  implicit val executionContext: ExecutionContext = system.dispatcher
  
  private val log = system.log
  
  private val interface = sys.env.getOrElse("BIND_INTERFACE", "0.0.0.0")
  private val baseDir = sys.env.getOrElse("WWW_DIR", "/var/www/hosts")
  
  private val hostsDir = s"$baseDir/hosts"
  
  private val hostsMap: TrieMap[String, Route] = getHostToRouteMap(hostsDir)
    
  private val route =
    redirectToNoWwwHost {
      redirectToNoTrailingSlash {
        getHostRoute(hostsMap)
      }
    }
  
  private val bindingFuture = Http().newServerAt(interface, 8080).bind(route)
  
  sys.addShutdownHook {
    bindingFuture
      .flatMap(_.unbind()) // trigger unbinding from the port
      .onComplete { _ =>
        log.info("Unbinding complete. shutting down akka system")
        system.terminate() // and shutdown when done
      }
  }
}
