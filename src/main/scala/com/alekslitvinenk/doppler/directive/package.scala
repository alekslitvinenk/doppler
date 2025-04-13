package com.alekslitvinenk.doppler

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server._
import akka.http.scaladsl.server.Directives.{redirectToNoTrailingSlashIfPresent, _}

import scala.collection.concurrent.TrieMap

package object directive {
  
  val redirectToNoWwwHost: Directive0 = extractUri.flatMap { uri =>
    val host = uri.authority.host.address()
    
    if (host.startsWith("www.")) {
      val redirectHost = host.substring(4)
      
      redirect(uri.withHost(redirectHost), StatusCodes.MovedPermanently)
    } else {
      pass
    }
  }
  
  val redirectToNoTrailingSlash: Directive0 = redirectToNoTrailingSlashIfPresent(StatusCodes.MovedPermanently)
  
  def getHostRoute(map: TrieMap[String, Route]): Route =
    extractHost { host =>
      map.get(host) match {
        case Some(value) => value
        case None => reject
      }
    }
}
