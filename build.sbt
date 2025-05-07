
val circeVersion = "0.14.12"

val circeLibs = Seq(
  "io.circe" %% "circe-core",
  "io.circe" %% "circe-generic",
  "io.circe" %% "circe-parser"
).map(_ % circeVersion)

lazy val root = (project in file("."))
  .settings(
    organization := "com.alekslitvinenk",
    name := "doppler",
    version := "0.1",
    scalaVersion := "2.13.16",
    
    //Compile / mainClass := Some("com.alekslitvinenk.doppler.Main"),

    libraryDependencies ++= Seq(
      "com.typesafe.akka" %% "akka-http" % "10.1.11",
      "com.typesafe.akka" %% "akka-stream" % "2.6.3",
      "com.typesafe.akka" %% "akka-http-spray-json" % "10.1.11",
      "com.typesafe.akka" %% "akka-slf4j" % "2.6.3",
      "ch.qos.logback" % "logback-classic" % "1.2.3",
      "org.scalatest" %% "scalatest" % "3.1.3" % Test,
      "org.scalamock" %% "scalamock" % "4.4.0" % Test,
    ) ++ circeLibs,
    
    Compile / unmanagedResourceDirectories += { baseDirectory.value / "src/main/resources" },
    excludeFilter := HiddenFileFilter -- ".well-known"
  )

addCommandAlias(
  "build",
  """|;
     |clean;
     |assembly;
  """.stripMargin)