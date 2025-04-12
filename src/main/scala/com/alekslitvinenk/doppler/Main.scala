package com.alekslitvinenk.doppler

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.model.Uri.{Path => AkkaPath}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.{PathMatcher, Route}
import akka.stream.ActorMaterializer
import com.alekslitvinenk.doppler.directive.redirectToNoWwwIfPresent
import io.circe.generic.auto._
import io.circe.parser._

import java.nio.file._
import java.nio.file.attribute.BasicFileAttributes
import scala.collection.concurrent.TrieMap
import scala.concurrent.ExecutionContext
import scala.jdk.CollectionConverters.CollectionHasAsScala

object Main extends App {

  implicit val system: ActorSystem = ActorSystem("my-system")
  implicit val executionContext: ExecutionContext = system.dispatcher
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  
  private val log = system.log
  
  private val interface = sys.env.getOrElse("BIND_INTERFACE", "0.0.0.0")
  private val baseDir = sys.env.getOrElse("WWW_DIR", "/var/www/hosts")
  //private val baseDir = sys.env.getOrElse("WWW_DIR", "/Users/aleksandrlitvinenko/dummy-hosts")
  
  private val hostsDir = s"$baseDir/hosts"
  
  private def getPageDataFiles(hostPath: String): List[PageData] = {
    import scala.collection.mutable.ArrayBuffer
    
    val pages = ArrayBuffer.empty[PageData]
    val root = Paths.get(hostPath)
    
    Files.walkFileTree(root, new SimpleFileVisitor[Path] {
      override def visitFile(file: Path, attrs: BasicFileAttributes): FileVisitResult = {
        if (file.getFileName.toString == "page-data.json") {
          decode[PageData](Files.readString(file)) match {
            case Left(_) => println(s"Couldn't read PageData from file [$file]")
            case Right(value) => pages += value
          }
        }
        FileVisitResult.CONTINUE
      }
    })
    
    pages.toList
  }
  
  private def getHostRoutes(hostPath: Path) = {
    val hostPathStr = hostPath.toString
    val pages = getPageDataFiles(hostPathStr)
      .map(_.path.stripSuffix("/").stripPrefix("/"))
      .filter(!_.isBlank)
      /*.map { p =>
        val numSegments = p.split("/").length
        (p, numSegments)
      }.sortBy(_._2)
      .map(_._1)*/
    
    val hostPagesRoute = pages.foldLeft(pathSingleSlash {
      getFromFile(s"$hostPathStr/index.html")
    }) { (route, pagePath) =>
      val pageSegments = pagePath.split("/").toList
      val headSegment = PathMatcher(pageSegments.head :: AkkaPath.Empty, ())
      val pathMatcher = pageSegments.tail.foldLeft(headSegment) { (pm, s) =>
        pm / s
      }
      
      route ~ path(pathMatcher) {
        getFromFile(s"$hostPathStr/$pagePath/index.html")
      }
    }
    
    val hostName = hostPath.getFileName.toString
    
    hostName -> host(hostName) {
      get {
        hostPagesRoute ~ {
          getFromDirectory(hostPathStr)
        }
      }
    }
  }
  
  private val hostsPath = Paths.get(hostsDir)
  private val allHosts = Files.list(hostsPath).toList
    .asScala.filter { p =>
    p.toFile.isDirectory
  }
  
  private val mmp = allHosts.map(getHostRoutes).toMap
  
  private val hostsMap: TrieMap[String, Route] = TrieMap.from(mmp)
    
  private val route2 = redirectToNoWwwIfPresent {
    redirectToNoTrailingSlashIfPresent(StatusCodes.MovedPermanently) {
      extractHost { host =>
        hostsMap(host)
      }
    }
  }
  
  private val bindingFuture = Http().bindAndHandle(route2, interface, 8080)
  
  sys.addShutdownHook {
    bindingFuture
      .flatMap(_.unbind()) // trigger unbinding from the port
      .onComplete { _ =>
        log.info("Unbinding complete. shutting down akka system")
        system.terminate() // and shutdown when done
      }
  }
}
