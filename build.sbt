
lazy val root = (project in file("."))
  .settings(
    organization := "com.alekslitvinenk",
    name := "doppler",
    version := "0.1",
    scalaVersion := "2.12.8",

    mainClass in (Compile, run) := Some("com.alekslitvinenk.doppler.Main"),

    libraryDependencies ++= Seq(
      "com.typesafe.akka" %% "akka-http" % "10.1.11",
      "com.typesafe.akka" %% "akka-stream" % "2.6.3",
      "com.typesafe.akka" %% "akka-http-spray-json" % "10.1.11",
      "com.typesafe.akka" %% "akka-slf4j" % "2.6.3",
      "ch.qos.logback" % "logback-classic" % "1.2.6",
      "org.scalatest" %% "scalatest" % "3.1.0" % Test,
      "org.scalamock" %% "scalamock" % "4.4.0" % Test,
    ),

    unmanagedResourceDirectories in Compile += { baseDirectory.value / "src/main/resources" },
    excludeFilter := HiddenFileFilter -- ".well-known"
  )

addCommandAlias(
  "build",
  """|;
     |clean;
     |assembly;
  """.stripMargin)