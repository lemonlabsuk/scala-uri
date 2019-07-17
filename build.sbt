import sbt.Keys.libraryDependencies
import sbtcrossproject.CrossPlugin.autoImport.{crossProject, CrossType}
import MimaSettings.mimaSettings

name                            := "scala-uri root"
scalaVersion in ThisBuild       := "2.13.0"
crossScalaVersions in ThisBuild := Seq("2.12.8", "2.13.0")

val sharedSettings = Seq(
  name          := "scala-uri",
  organization  := "io.lemonlabs",
  scalacOptions := Seq("-unchecked", "-deprecation", "-encoding", "utf8", "-feature", "-Xfatal-warnings"),
  libraryDependencies ++= Seq(
    "org.parboiled" %%% "parboiled" % "2.1.7",
    "com.chuusai"   %%% "shapeless" % "2.3.3",
    "org.scalatest" %%% "scalatest" % "3.0.8" % "test"
  ),
  parallelExecution in Test := false
)

val jvmSettings = Seq(
  libraryDependencies ++= Seq(
    "io.spray" %%  "spray-json" % "1.3.5" % Optional
  )
)

val publishingSettings = Seq(
  publishMavenStyle := true,
  publishArtifact in Test := false,
  pomIncludeRepository := { _ => false },
  resolvers += "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots",
  resolvers += "Sonatype OSS Releases" at "https://oss.sonatype.org/content/repositories/releases",
  publishTo := {
    val nexus = "https://oss.sonatype.org/"
    if (version.value.trim.endsWith("SNAPSHOT"))
      Some("snapshots" at nexus + "content/repositories/snapshots")
    else
      Some("releases"  at nexus + "service/local/staging/deploy/maven2")
  },
  pomExtra :=
    <url>https://github.com/lemonlabsuk/scala-uri</url>
      <licenses>
        <license>
          <name>Apache 2</name>
          <url>http://www.apache.org/licenses/LICENSE-2.0</url>
          <distribution>repo</distribution>
        </license>
      </licenses>
      <scm>
        <url>git@github.com:lemonlabsuk/scala-uri.git</url>
        <connection>scm:git@github.com:lemonlabsuk/scala-uri.git</connection>
      </scm>
      <developers>
        <developer>
          <id>theon</id>
          <name>Ian Forsey</name>
          <url>https://lemonlabs.io</url>
        </developer>
      </developers>
)

lazy val scalaUri =
  crossProject(JSPlatform, JVMPlatform)
    .crossType(CrossType.Full)
    .in(file("."))
    .settings(sharedSettings)
    .settings(publishingSettings)
    .settings(mimaSettings)
    .jvmSettings(jvmSettings)

lazy val updatePublicSuffixes = taskKey[Unit]("Updates the public suffix Trie at io.lemonlabs.uri.internet.PublicSuffixes")

updatePublicSuffixes := UpdatePublicSuffixTrie.generate()
