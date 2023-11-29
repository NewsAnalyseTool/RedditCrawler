ThisBuild / scalaVersion := "2.13.12"
ThisBuild / version := "0.1.0-SNAPSHOT"
ThisBuild / organization := "com.example"
ThisBuild / organizationName := "example"

name := "reddit-crawler-prototype"

libraryDependencies ++= Seq(
  "org.scalameta" %% "munit" % "0.7.29" % Test,
  "com.softwaremill.sttp.client3" %% "core" % "3.8.13",
  "com.typesafe.play" %% "play-json" % "2.10.0",
  "com.typesafe" % "config" % "1.4.2",
  // Using MongoDB Scala driver
  "org.mongodb.scala" %% "mongo-scala-driver" % "4.3.3",
  "org.slf4j" % "slf4j-api" % "2.0.5",
  "org.slf4j" % "slf4j-simple" % "2.0.5",
  "ch.qos.logback" % "logback-classic" % "1.4.7",
  "org.slf4j" % "slf4j-api" % "2.0.5"
)




