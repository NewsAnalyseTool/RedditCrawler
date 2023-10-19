import Dependencies._

ThisBuild / scalaVersion     := "2.13.12"
ThisBuild / version          := "0.1.0-SNAPSHOT"
ThisBuild / organization     := "com.example"
ThisBuild / organizationName := "example"

lazy val root = (project in file("."))
  .settings(
    name := "reddit-crawler-prototype",
    libraryDependencies += munit % Test,
    libraryDependencies += "com.softwaremill.sttp.client3" %% "core" % "3.8.13",
    libraryDependencies += "com.typesafe.play" %% "play-json" % "2.10.0",
    libraryDependencies += "com.typesafe" % "config" % "1.4.2"
  )

// See https://www.scala-sbt.org/1.x/docs/Using-Sonatype.html for instructions on how to publish to Sonatype.



