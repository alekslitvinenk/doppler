package com.alekslitvinenk.doppler.util

import akka.http.scaladsl.model.Uri.{Path => AkkaPath}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.{PathMatcher, Route}
import com.alekslitvinenk.doppler.domain.GatsbyPageData
import io.circe.generic.auto._
import io.circe.parser.decode

import java.nio.file._
import java.nio.file.attribute.BasicFileAttributes
import scala.collection.concurrent.TrieMap
import scala.collection.mutable.ArrayBuffer
import scala.jdk.CollectionConverters.CollectionHasAsScala

object Utils {
  
  def getHostToRouteMap(hostsDir: String): TrieMap[String, Route] = {
    val hostsPath = Paths.get(hostsDir)
    val allHosts = Files.list(hostsPath).toList
      .asScala.filter { p =>
        p.toFile.isDirectory
      }
    
    TrieMap.from(allHosts.map(h => h.getFileName.toString -> getPageRoutesByHostPath(h)).toMap)
  }
  
  private def getPageRoutesByHostPath(hostPath: Path): Route = {
    val hostPathStr = hostPath.toString
    val pages = getPageDataFiles(hostPathStr)
      .map(_.path.stripSuffix("/").stripPrefix("/"))
      .filter(!_.isBlank)
    
    val pagesRoute = pages.foldLeft(pathSingleSlash {
      getFromFile(s"$hostPathStr/index.html")
    }) { (route, pagePath) =>
      val pathSegments = pagePath.split("/").toList
      val headSegment = PathMatcher(pathSegments.head :: AkkaPath.Empty, ())
      val pathMatcher = pathSegments.tail.foldLeft(headSegment) { (pm, s) =>
        pm / s
      }
      
      route ~ path(pathMatcher) {
        getFromFile(s"$hostPathStr/$pagePath/index.html")
      }
    }
    
    val hostName = hostPath.getFileName.toString
    
    host(hostName) {
      get {
        encodeResponse {
          pagesRoute ~ {
            getFromDirectory(hostPathStr)
          }
        }
      }
    }
  }
  
  private def getPageDataFiles(hostPath: String): List[GatsbyPageData] = {
    val pages = ArrayBuffer.empty[GatsbyPageData]
    val root = Paths.get(hostPath)
    
    Files.walkFileTree(root, new SimpleFileVisitor[Path] {
      override def visitFile(file: Path, attrs: BasicFileAttributes): FileVisitResult = {
        if (file.getFileName.toString == "page-data.json") {
          decode[GatsbyPageData](Files.readString(file)) match {
            case Left(_) => println(s"Couldn't read PageData from file [$file]")
            case Right(value) => pages += value
          }
        }
        FileVisitResult.CONTINUE
      }
    })
    
    pages.toList
  }
}
