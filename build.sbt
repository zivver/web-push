
name := "web-push"

organization := "com.zivver"

version := "0.1.0"

scalaVersion := "2.11.8"

libraryDependencies ++= Seq(
  "com.pauldijou" %% "jwt-core" % "0.9.0",
  "org.apache.httpcomponents" % "fluent-hc" % "4.5.2",
  "org.bouncycastle" % "bcprov-jdk15on" % "1.55"
)
