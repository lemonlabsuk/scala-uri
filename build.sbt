import sbt.Keys.libraryDependencies
import sbtcrossproject.CrossPlugin.autoImport.{crossProject, CrossType}
import MimaSettings.mimaSettings

import scala.xml.transform.{RewriteRule, RuleTransformer}

name                            := "scala-uri root"
scalaVersion in ThisBuild       := "2.13.1"
crossScalaVersions in ThisBuild := Seq("2.12.10", scalaVersion.value)

lazy val paradisePlugin = Def.setting {
  CrossVersion.partialVersion(scalaVersion.value) match {
    case Some((2, v)) if v <= 12 =>
      Seq(compilerPlugin("org.scalamacros" % "paradise" % "2.1.1" cross CrossVersion.patch))
    case _ =>
      // if scala 2.13.0-M4 or later, macro annotations merged into scala-reflect
      // https://github.com/scala/scala/pull/6606
      Nil
  }
}

val sharedSettings = Seq(
  name         := "scala-uri",
  organization := "io.lemonlabs",
  scalacOptions := Seq(
    "-unchecked",
    "-deprecation",
    "-encoding",
    "utf8",
    "-feature",
    "-Xfatal-warnings",
    "-language:higherKinds"
  )
    ++ (if (scalaVersion.value.startsWith("2.13")) Seq("-Ymacro-annotations") else Nil),
  libraryDependencies ++= Seq(
    "org.parboiled" %%% "parboiled"  % "2.1.8",
    "com.chuusai"   %%% "shapeless"  % "2.3.3",
    "org.typelevel" %%% "simulacrum" % "1.0.0" % Provided,
    "org.scalatest" %%% "scalatest"  % "3.1.0" % Test
  ),
  libraryDependencies ++= paradisePlugin.value,
  parallelExecution in Test := false,
  scalafmtOnCompile         := true,
  pomPostProcess := { node =>
    new RuleTransformer(new RewriteRule {
      override def transform(node: xml.Node): Seq[xml.Node] = node match {
        case e: xml.Elem
            if e.label == "dependency" &&
              e.child.exists(child => child.label == "groupId" && child.text == "org.typelevel") &&
              e.child.exists(child => child.label == "artifactId" && child.text.startsWith("simulacrum_")) =>
          Nil
        case _ => Seq(node)
      }
    }).transform(node).head
  }
)

val jvmSettings = Seq(
  libraryDependencies ++= Seq(
    "io.spray" %% "spray-json" % "1.3.5" % Optional
  )
)

val publishingSettings = Seq(
  description             := "Simple scala library for building and parsing URIs",
  publishMavenStyle       := true,
  publishArtifact in Test := false,
  pomIncludeRepository := { _ =>
    false
  },
  resolvers += "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots",
  resolvers += "Sonatype OSS Releases" at "https://oss.sonatype.org/content/repositories/releases",
  publishTo := {
    val nexus = "https://oss.sonatype.org/"
    if (version.value.trim.endsWith("SNAPSHOT"))
      Some("snapshots" at nexus + "content/repositories/snapshots")
    else
      Some("releases" at nexus + "service/local/staging/deploy/maven2")
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

lazy val updatePublicSuffixes =
  taskKey[Unit]("Updates the public suffix Trie at io.lemonlabs.uri.internet.PublicSuffixes")

updatePublicSuffixes := UpdatePublicSuffixTrie.generate()

addCommandAlias("check", ";scalafmtCheckAll;scalafmtSbtCheck")
addCommandAlias("fmt", ";scalafmtAll;scalafmtSbt")
