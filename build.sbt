name := """play-chat"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.8"

val specsV = "3.7.2"
val scalaTestV = "2.2.6"

val mongoDeps = Seq(
  "org.scalatest" %% "scalatest" % scalaTestV,
  "org.specs2" %% "specs2-core" % specsV,
  "org.specs2" %% "specs2-matcher-extra" % specsV,
  "org.mongodb" %% "casbah" % "3.1.1",
  "com.typesafe" % "config" % "1.3.0"
)

libraryDependencies ++= Seq(
  jdbc,
  cache,
  ws,
  specs2 % Test
)

libraryDependencies ++= mongoDeps


resolvers += "scalaz-bintray" at "http://dl.bintray.com/scalaz/releases"

// Play provides two styles of routers, one expects its actions to be injected, the
// other, legacy style, accesses its actions statically.
routesGenerator := InjectedRoutesGenerator
