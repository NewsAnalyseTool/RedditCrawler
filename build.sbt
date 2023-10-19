ThisBuild / scalaVersion     := "2.13.12"
ThisBuild / version          := "0.1.0-SNAPSHOT"
ThisBuild / organization     := "com.example"
ThisBuild / organizationName := "example"

lazy val root = (project in file("."))
  .settings(
      name := "reddit-crawler-prototype",
      libraryDependencies ++= Seq(
          "org.scalameta" %% "munit" % "0.7.29" % Test,  // Replace "0.x.x" with your desired version
          "com.softwaremill.sttp.client3" %% "core" % "3.8.13",
          "com.typesafe.play" %% "play-json" % "2.10.0",
          "com.typesafe" % "config" % "1.4.2"
      )
  )




