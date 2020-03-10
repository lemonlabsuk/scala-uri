import sbt.Keys.libraryDependencies
import sbtcrossproject.CrossPlugin.autoImport.{crossProject, CrossType}
import MimaSettings.mimaSettings

import scala.xml.transform.{RewriteRule, RuleTransformer}

name                            := "scala-uri root"
scalaVersion in ThisBuild       := "2.13.1"
crossScalaVersions in ThisBuild := Seq("2.12.10", scalaVersion.value)
skip in publish                 := true // Do not publish the root project

lazy val paradisePlugin = Def.setting {
  VersionNumber(scalaVersion.value) match {
    case v if v.matchesSemVer(SemanticSelector("<2.13.0-M4")) =>
      Seq(compilerPlugin("org.scalamacros" % "paradise" % "2.1.1" cross CrossVersion.patch))
    case _ =>
      // if scala 2.13.0-M4 or later, macro annotations merged into scala-reflect
      // https://github.com/scala/scala/pull/6606
      Nil
  }
}

val sharedSettings = Seq(
  organization := "io.lemonlabs",
  libraryDependencies ++= Seq(
    compilerPlugin("com.github.ghik" % "silencer-plugin" % "1.6.0" cross CrossVersion.full),
    "com.github.ghik"   % "silencer-lib"      % "1.6.0"   % Provided cross CrossVersion.full,
    "org.scalatest"     %%% "scalatest"       % "3.1.1"   % Test,
    "org.scalatestplus" %%% "scalacheck-1-14" % "3.1.1.1" % Test,
    "org.scalacheck"    %%% "scalacheck"      % "1.14.3"  % Test,
    "org.typelevel"     %%% "cats-laws"       % "2.1.1"   % Test
  ),
  scalacOptions := Seq(
    "-unchecked",
    "-deprecation",
    "-encoding",
    "utf8",
    "-feature",
    "-Xfatal-warnings",
    "-language:higherKinds",
    // Silence warnings for deprecated scala-uri code
    "-P:silencer:pathFilters=.*io/lemonlabs/uri/dsl/package.scala;.*io/lemonlabs/uri/DslTests.scala;.*io/lemonlabs/uri/DslTypeTests.scala"
  ) ++ (
    VersionNumber(scalaVersion.value) match {
      case v if v.matchesSemVer(SemanticSelector(">=2.13")) => Seq("-Ymacro-annotations")
      case v if v.matchesSemVer(SemanticSelector("<=2.12")) => Seq("-Ypartial-unification")
      case _                                                => Nil
    }
  ),
  parallelExecution in Test := false,
  scalafmtOnCompile         := true,
  coverageExcludedPackages  := "io.lemonlabs.uri.inet.PublicSuffixTrie.*"
)

val scalaUriSettings = Seq(
  name        := "scala-uri",
  description := "Simple scala library for building and parsing URIs",
  libraryDependencies ++= Seq(
    "org.parboiled" %%% "parboiled"  % "2.1.8",
    "com.chuusai"   %%% "shapeless"  % "2.3.3",
    "org.typelevel" %%% "simulacrum" % "1.0.0" % Provided,
    "org.typelevel" %%% "cats-core"  % "2.1.1"
  ),
  libraryDependencies ++= paradisePlugin.value,
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

val publishingSettings = Seq(
  publishMavenStyle       := true,
  skip in publish         := false,
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
    .settings(scalaUriSettings)
    .settings(publishingSettings)
    .settings(mimaSettings)
    .jsSettings(
      libraryDependencies += "org.scala-js" %%% "scalajs-dom" % "1.0.0"
    )

lazy val docs = project
  .in(file("scala0-uri-docs"))
  .settings(
    // README.md has examples with expected compiler warnings (deprecated code, exhaustive matches)
    // Turn off these warnings to keep this noise down
    // We can remove this if the following is implemented https://github.com/scalameta/mdoc/issues/286
    scalacOptions   := Seq("--no-warnings"),
    skip in publish := true,
    publishArtifact := false
  )
  .dependsOn(scalaUri.jvm)
  .enablePlugins(MdocPlugin)

lazy val updatePublicSuffixes =
  taskKey[Unit]("Updates the public suffix Trie at io.lemonlabs.uri.internet.PublicSuffixes")

updatePublicSuffixes := UpdatePublicSuffixTrie.generate()

lazy val testPublicSuffixes =
  taskKey[Unit](
    "Makes a small public suffix Trie at io.lemonlabs.uri.internet.PublicSuffixes which can be use to run the tests and can be instrumented without exceeding JVM class size limits"
  )

testPublicSuffixes := UpdatePublicSuffixTrie.generateTestVersion()

addCommandAlias("check", ";scalafmtCheckAll;scalafmtSbtCheck")
addCommandAlias("fmt", ";scalafmtAll;scalafmtSbt")
