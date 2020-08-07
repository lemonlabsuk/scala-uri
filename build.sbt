import sbt.Keys.libraryDependencies
import sbtcrossproject.CrossPlugin.autoImport.{crossProject, CrossType}

import scala.xml.transform.{RewriteRule, RuleTransformer}
import com.typesafe.tools.mima.core.{
  DirectMissingMethodProblem,
  MissingClassProblem,
  ProblemFilters,
  ReversedMissingMethodProblem
}
import com.typesafe.tools.mima.plugin.MimaKeys.{mimaBinaryIssueFilters, mimaPreviousArtifacts, mimaReportBinaryIssues}

name                            := "scala-uri root"
scalaVersion in ThisBuild       := "2.13.2"
crossScalaVersions in ThisBuild := Seq("2.12.11", scalaVersion.value)
skip in publish                 := true // Do not publish the root project

val simulacrumScalafixVersion = "0.5.0"
scalafixDependencies in ThisBuild += "org.typelevel" %% "simulacrum-scalafix" % simulacrumScalafixVersion

val sharedSettings = Seq(
  organization := "io.lemonlabs",
  libraryDependencies ++= Seq(
    compilerPlugin("com.github.ghik" % "silencer-plugin" % "1.7.1" cross CrossVersion.full),
    "com.github.ghik"     % "silencer-lib"                    % "1.7.1" cross CrossVersion.full,
    "org.typelevel"     %%% "simulacrum-scalafix-annotations" % "0.5.0",
    "org.scalatest"     %%% "scalatest"                       % "3.2.1"   % Test,
    "org.scalatestplus" %%% "scalacheck-1-14"                 % "3.2.1.0" % Test,
    "org.scalacheck"    %%% "scalacheck"                      % "1.14.3"  % Test,
    "org.typelevel"     %%% "cats-laws"                       % "2.1.1"   % Test
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
  addCompilerPlugin(scalafixSemanticdb),
  scalacOptions ++= Seq(s"-P:semanticdb:targetroot:${baseDirectory.value}/target/.semanticdb", "-Yrangepos"),
  parallelExecution in Test := false,
  scalafmtOnCompile         := true,
  coverageExcludedPackages  := "(io.lemonlabs.uri.inet.Trie.*|io.lemonlabs.uri.inet.PublicSuffixes.*|io.lemonlabs.uri.inet.PublicSuffixTrie.*|io.lemonlabs.uri.inet.PunycodeSupport.*)"
)

def removePomDependency(groupId: String, artifactIdPrefix: String): PartialFunction[xml.Node, Seq[xml.Node]] = {
  case e: xml.Elem
      if e.label == "dependency" &&
        e.child.exists(child => child.label == "groupId" && child.text == groupId) &&
        e.child.exists(child => child.label == "artifactId" && child.text.startsWith(artifactIdPrefix)) =>
    Nil
}

val scalaUriSettings = Seq(
  name        := "scala-uri",
  description := "Simple scala library for building and parsing URIs",
  libraryDependencies ++= Seq(
    "org.parboiled" %%% "parboiled" % "2.2.0",
    "com.chuusai"   %%% "shapeless" % "2.3.3",
    "org.typelevel" %%% "cats-core" % "2.1.1"
  ),
  pomPostProcess := { node =>
    new RuleTransformer(new RewriteRule {
      override def transform(node: xml.Node): Seq[xml.Node] = {
        val removeUnneededPomDeps =
          removePomDependency(groupId = "org.typelevel", artifactIdPrefix = "simulacrum") orElse
            removePomDependency(groupId = "com.github.ghik", artifactIdPrefix = "silencer-lib")

        removeUnneededPomDeps.applyOrElse(node, (_: xml.Node) => Seq(node))
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

val previousVersions = (0 to 2).map(v => s"2.$v.0").toSet

val mimaExcludes = Seq(
  ProblemFilters.exclude[ReversedMissingMethodProblem]("io.lemonlabs.uri.typesafe.QueryValueInstances1.*")
)

val mimaSettings = Seq(
  mimaPreviousArtifacts := {
    if (VersionNumber(scalaVersion.value).matchesSemVer(SemanticSelector("<=2.13")))
      previousVersions.map { organization.value % s"${name.value}_${scalaBinaryVersion.value}" % _ }
    else
      Set.empty
  },
  mimaBinaryIssueFilters ++= {
    VersionNumber(scalaVersion.value) match {
      case v if v.matchesSemVer(SemanticSelector("<2.13")) =>
        mimaExcludes ++ Seq(
          // In scala 2.12 adding a method to a value class breaks binary compatibility (see here: https://github.com/lightbend/mima/issues/135).
          // This was fixed in scala 2.13, which is why we only exclude from mima for 2.12
          ProblemFilters.exclude[DirectMissingMethodProblem]("io.lemonlabs.uri.typesafe.dsl.TypesafeUrlDsl.*")
        )
      case _ => mimaExcludes
    }
  },
  mimaBinaryIssueFilters ++= Seq(
    // Exclude the autogenerated public suffix Trie
    ProblemFilters.exclude[MissingClassProblem]("io.lemonlabs.uri.inet._*")
  ),
  test in Test := {
    mimaReportBinaryIssues.value
    mimaBinaryIssueFilters.value
    (test in Test).value
  }
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
    .jsSettings(
      //scalac-scoverage-plugin Scala.js 1.0 is not yet released.
      coverageEnabled := false
    )

lazy val docs = project
  .in(file("scala-uri-docs"))
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
  taskKey[Unit]("Updates the public suffix Set at io.lemonlabs.uri.internet.PublicSuffixes")

updatePublicSuffixes := UpdatePublicSuffixes.generate()

addCommandAlias("check", ";scalafmtCheckAll;scalafmtSbtCheck")
addCommandAlias("fmt", ";scalafmtAll;scalafmtSbt")
