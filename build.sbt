import sbt.Keys.libraryDependencies
import sbtcrossproject.CrossPlugin.autoImport.{crossProject, CrossType}

import scala.xml.transform.{RewriteRule, RuleTransformer}
import com.typesafe.tools.mima.core.{
  DirectMissingMethodProblem,
  IncompatibleResultTypeProblem,
  MissingClassProblem,
  ProblemFilters,
  ReversedMissingMethodProblem
}
import com.typesafe.tools.mima.plugin.MimaKeys.{mimaBinaryIssueFilters, mimaPreviousArtifacts, mimaReportBinaryIssues}

name := "scala-uri root"

ThisBuild / scalaVersion       := "3.1.3"
ThisBuild / crossScalaVersions := Seq("2.12.15", "2.13.8", scalaVersion.value)
publish / skip                 := true // Do not publish the root project

val simulacrumScalafixVersion = "0.5.4"
ThisBuild / scalafixDependencies += "org.typelevel" %% "simulacrum-scalafix" % simulacrumScalafixVersion

val isScala3 = Def.setting {
  CrossVersion.partialVersion(scalaVersion.value).exists(_._1 != 2)
}

val sharedSettings = Seq(
  organization := "io.lemonlabs",
  libraryDependencies ++= Seq(
    "org.typelevel"     %%% "simulacrum-scalafix-annotations" % simulacrumScalafixVersion,
    "org.scalatest"     %%% "scalatest"                       % "3.2.12"   % Test,
    "org.scalatestplus" %%% "scalacheck-1-15"                 % "3.2.11.0" % Test,
    "org.scalacheck"    %%% "scalacheck"                      % "1.16.0"   % Test,
    "org.typelevel"     %%% "cats-laws"                       % "2.7.0"    % Test
  ),
  scalacOptions ++= Seq(
    "-unchecked",
    "-deprecation",
    "-encoding",
    "utf8",
    "-feature",
    "-Xfatal-warnings",
    "-language:higherKinds,implicitConversions"
  ) ++ (
    VersionNumber(scalaVersion.value) match {
      case v if v.matchesSemVer(SemanticSelector("=2.13"))  => Seq("-Ymacro-annotations")
      case v if v.matchesSemVer(SemanticSelector("<=2.12")) => Seq("-Ypartial-unification")
      case _                                                => Nil
    }
  ),
  semanticdbEnabled        := true,
  semanticdbVersion        := scalafixSemanticdb.revision,
  Test / parallelExecution := false,
  coverageExcludedPackages := "(io.lemonlabs.uri.inet.Trie.*|io.lemonlabs.uri.inet.PublicSuffixes.*|io.lemonlabs.uri.inet.PublicSuffixTrie.*|io.lemonlabs.uri.inet.PunycodeSupport.*)"
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
    "org.typelevel" %%% "cats-core"  % "2.7.0",
    "org.typelevel" %%% "cats-parse" % "0.3.7"
  ),
  libraryDependencies ++= (if (isScala3.value) Nil else Seq("com.chuusai" %%% "shapeless" % "2.3.9")),
  pomPostProcess := { node =>
    new RuleTransformer(new RewriteRule {
      override def transform(node: xml.Node): Seq[xml.Node] = {
        removePomDependency(groupId = "org.typelevel", artifactIdPrefix = "simulacrum")
          .applyOrElse(node, (_: xml.Node) => Seq(node))
      }
    }).transform(node).head
  }
)

val publishingSettings = Seq(
  publishMavenStyle      := true,
  publish / skip         := false,
  Test / publishArtifact := false,
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

val previousVersions = Set.empty[String] // Set(0, 4).map(v => s"3.$v.0")

val mimaExcludes = Seq(
  ProblemFilters.exclude[ReversedMissingMethodProblem]("io.lemonlabs.uri.typesafe.QueryValueInstances1.*"),
  ProblemFilters.exclude[ReversedMissingMethodProblem]("io.lemonlabs.uri.Host.*"),
  ProblemFilters.exclude[ReversedMissingMethodProblem]("io.lemonlabs.uri.Uri.*"),
  ProblemFilters.exclude[ReversedMissingMethodProblem]("io.lemonlabs.uri.Url.*"),
  ProblemFilters.exclude[ReversedMissingMethodProblem]("io.lemonlabs.uri.UrlPath.*")
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
  Test / test := (Test / test).dependsOn(mimaReportBinaryIssues, mimaBinaryIssueFilters).value
)

lazy val scalaUri =
  crossProject(JSPlatform, JVMPlatform)
    .crossType(CrossType.Full)
    .in(file("."))
    .settings(sharedSettings)
    .settings(scalaUriSettings)
    .settings(publishingSettings)
    .settings(mimaSettings)
    .jvmSettings(
      Test / fork := true
    )
    .jsSettings(
      libraryDependencies += "org.scala-js" %%% "scalajs-dom" % "2.2.0",
      libraryDependencies ++= (
        // securerandom used by scoverage in scala 2 tests
        if (isScala3.value) Nil
        else Seq("org.scala-js" %%% "scalajs-java-securerandom" % "1.0.0" % Test)
      )
    )

lazy val docs = project
  .in(file("scala-uri-docs"))
  .settings(
    // README.md has examples with expected compiler warnings (deprecated code, exhaustive matches)
    // Turn off these warnings to keep this noise down
    // We can remove this if the following is implemented https://github.com/scalameta/mdoc/issues/286
    scalacOptions ++= Seq("--no-warnings"),
    publish / skip  := true,
    publishArtifact := false
  )
  .dependsOn(scalaUri.jvm)
  .enablePlugins(MdocPlugin)

lazy val updatePublicSuffixes =
  taskKey[Unit]("Updates the public suffix Set at io.lemonlabs.uri.internet.PublicSuffixes")

updatePublicSuffixes := UpdatePublicSuffixes.generate()

addCommandAlias("check", ";scalafmtCheckAll;scalafmtSbtCheck")
addCommandAlias("fmt", ";scalafmtAll;scalafmtSbt")
