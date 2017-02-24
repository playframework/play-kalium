name := """play-scala-secure-session-example"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.11"

libraryDependencies += "org.abstractj.kalium" % "kalium" % "0.4.0"
libraryDependencies += cache
libraryDependencies += filters

libraryDependencies += ws % Test
libraryDependencies += "org.scalatestplus.play" %% "scalatestplus-play" % "2.0.0" % Test
