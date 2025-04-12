package com.alekslitvinenk.doppler

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server._
import akka.http.scaladsl.server.Directives._

package object directive {
  //type PageRoute = Directive1[String]
  
  //def getHostPageRoute(host: String): PageRoute = getFromFile(s"$host")
  
  def redirectToNoWwwIfPresent: Directive0 = extractUri.flatMap { uri =>
    val host = uri.authority.host.address()
    
    if (host.startsWith("www.")) {
      val redirectHost = host.substring(4)
      
      redirect(uri.withHost(redirectHost), StatusCodes.MovedPermanently)
    } else {
      pass
    }
  }
}
