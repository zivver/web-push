
name := "web-push"

organization := "com.zivver"

version := "0.2.4"

scalaVersion := "2.12.6"

crossScalaVersions := Seq("2.11.11", "2.12.6")

libraryDependencies ++= Seq(
  "com.pauldijou" %% "jwt-core" % "1.0.0",
  "org.apache.httpcomponents" % "fluent-hc" % "4.5.6",
  "org.bouncycastle" % "bcprov-jdk15on" % "1.60"
)

publishTo := {
  val nexus = "https://oss.sonatype.org/"
  if (isSnapshot.value)
    Some("snapshots" at nexus + "content/repositories/snapshots")
  else
    Some("releases" at nexus + "service/local/staging/deploy/maven2")
}

publishMavenStyle := true

publishArtifact in Test := false

pomIncludeRepository := { _ => false }

pomExtra := (
  <url>https://github.com/zivver/web-push</url>
    <licenses>
      <license>
        <name>MIT License</name>
        <url>http://www.opensource.org/licenses/mit-license.php</url>
        <distribution>repo</distribution>
      </license>
    </licenses>
    <scm>
      <url>git@github.com:zivver/web-push.git</url>
      <connection>scm:git:git@github.com:zivver/web-push.git</connection>
    </scm>
    <developers>
      <developer>
        <id>zivver</id>
        <name>Zivver</name>
        <url>https://www.zivver.com</url>
      </developer>
    </developers>
  )
