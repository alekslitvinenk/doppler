
val circeVersion = "0.14.12"
val pekkoVersion = "1.1.5"

val circeLibs = Seq(
  "io.circe" %% "circe-core",
  "io.circe" %% "circe-generic",
  "io.circe" %% "circe-parser"
).map(_ % circeVersion)

val pekkoLibs = Seq(
  "org.apache.pekko" %% "pekko-actor",
  "org.apache.pekko" %% "pekko-stream"
).map(_ % pekkoVersion)

lazy val root = (project in file("."))
  .settings(
    organization := "com.alekslitvinenk",
    name := "doppler",
    version := "0.1",
    scalaVersion := "2.13.16",
    libraryDependencies ++= Seq(
      "org.apache.pekko" %% "pekko-http" % "1.2.0",
      "ch.qos.logback" % "logback-classic" % "1.2.3",
      "org.scalatest" %% "scalatest" % "3.2.19" % Test,
      "org.scalamock" %% "scalamock" % "7.4.1" % Test,
    ) ++ circeLibs ++ pekkoLibs,
    Compile / unmanagedResourceDirectories += { baseDirectory.value / "src/main/resources" },
    excludeFilter := HiddenFileFilter -- ".well-known"
  )

addCommandAlias(
  "build",
  """|;
     |clean;
     |assembly;
  """.stripMargin)