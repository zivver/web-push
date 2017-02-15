
name := "web-push"

organization := "com.zivver"

version := "0.1.1-SNAPSHOT"

scalaVersion := "2.12.1"

crossScalaVersions := Seq("2.11.8", "2.12.1")

libraryDependencies ++= Seq(
  "com.pauldijou" %% "jwt-core" % "0.10.0",
  "org.apache.httpcomponents" % "fluent-hc" % "4.5.2",
  "org.bouncycastle" % "bcprov-jdk15on" % "1.55"
)
