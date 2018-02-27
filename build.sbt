import sbt.Keys.libraryDependencies
import org.scalajs.sbtplugin.cross.CrossType

name                            := "scala-uri root"
scalaVersion in ThisBuild       := "2.12.4"
crossScalaVersions in ThisBuild := Seq("2.11.12", "2.12.4")

val sharedSettings = Seq(
  name          := "scala-uri",
  organization  := "io.lemonlabs",
  scalacOptions := Seq("-unchecked", "-deprecation", "-encoding", "utf8", "-feature", "-Xfatal-warnings"),
  libraryDependencies ++= Seq(
    "org.parboiled" %%% "parboiled" % "2.1.4",
    "com.chuusai"   %%% "shapeless" % "2.3.3",
    "org.scalatest" %%% "scalatest" % "3.0.4" % "test"
  ),
  parallelExecution in Test := false
)

val jvmSettings = Seq(
  libraryDependencies ++= Seq(
    "io.spray" %%  "spray-json" % "1.3.2"
  )
)

val publishingSettings = Seq(
  releasePublishArtifactsAction := PgpKeys.publishSigned.value,
  pgpSecretRing := file("secring.gpg"),
  pgpPublicRing := file("pubring.gpg"),
  pgpPassphrase := sys.env.get("PGP_PASSPHRASE").map(_.toCharArray),
  releaseProcess += releaseStepCommand("sonatypeRelease"),
  releaseCrossBuild := true,
  releaseTagComment := version.value,
  releaseCommitMessage := s"Bump version to ${version.value}",
  (for {
    username <- sys.env.get("SONATYPE_USERNAME")
    password <- sys.env.get("SONATYPE_PASSWORD")
  } yield
    credentials += Credentials(
      "Sonatype Nexus Repository Manager",
      "oss.sonatype.org",
      username,
      password)
  ).getOrElse(credentials ++= Seq()),
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
          <url>http://theon.github.io</url>
        </developer>
      </developers>
)

lazy val root = project.in(file("."))
  .aggregate(scalaUriJvm, scalaUriJs)
  .settings(
    publish := {},
    publishLocal := {}
  )

lazy val scalaUri =
  crossProject.in(file("."))
    .settings(sharedSettings)
    .settings(publishingSettings)
    .jvmSettings(jvmSettings)

lazy val scalaUriJs     = scalaUri.js
lazy val scalaUriJvm    = scalaUri.jvm

lazy val updatePublicSuffixes = taskKey[Unit]("Updates the public suffix Trie at com.netaporter.uri.internet.PublicSuffixes")

updatePublicSuffixes := UpdatePublicSuffixTrie.generate()
