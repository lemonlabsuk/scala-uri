import scala.xml.transform.{RewriteRule, RuleTransformer}
import com.typesafe.tools.mima.core.{
  DirectMissingMethodProblem,
  IncompatibleResultTypeProblem,
  MissingClassProblem,
  ProblemFilters,
  ReversedMissingMethodProblem
}
import com.typesafe.tools.mima.plugin.MimaKeys.{mimaBinaryIssueFilters, mimaPreviousArtifacts, mimaReportBinaryIssues}

val Versions = new {
  val Scala3 = "3.3.4"
  val Scala212 = "2.12.20"
  val Scala213 = "2.13.15"
  val scalajsDom = "2.6.0"

  val allScala = Seq(Scala3, Scala212, Scala213)
}

inThisBuild(
  List(
    organization     := "com.indoorvivants",
    organizationName := "Anton Sviridov",
    homepage := Some(
      url("https://github.com/indoorvivants/scala-uri")
    ),
    licenses := List(
      "Apache 2" -> url("http://www.apache.org/licenses/LICENSE-2.0")
    ),
    developers := List(
      Developer(
        "theon",
        "Ian Forsey",
        "",
        url("https://lemonlabs.io")
      )
    )
  )
)

lazy val root =
  project
    .in(file("."))
    .aggregate(scalaUri.projectRefs*)
    .settings(publish / skip := true, publishLocal / skip := true)

lazy val scalaUri =
  projectMatrix
    .in(file("core"))
    .settings(sharedSettings)
    .settings(scalaUriSettings)
    .settings(mimaSettings)
    .jvmPlatform(
      Versions.allScala,
      Seq(Test / fork := true)
    )
    .jsPlatform(
      Versions.allScala,
      Seq(
        libraryDependencies += "org.scala-js" %%% "scalajs-dom" % Versions.scalajsDom,
        libraryDependencies ++= (
          // securerandom used by scoverage in scala 2 tests
          if (isScala3.value) Nil
          else Seq("org.scala-js" %%% "scalajs-java-securerandom" % "1.0.0" % Test)
        )
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
    publishArtifact := false,
    scalaVersion    := Versions.Scala213
  )
  .dependsOn(scalaUri.jvm(Versions.Scala213))
  .enablePlugins(MdocPlugin)

val simulacrumScalafixVersion = "0.5.4"
ThisBuild / scalafixDependencies += "org.typelevel" %% "simulacrum-scalafix" % simulacrumScalafixVersion

val isScala3 = Def.setting {
  CrossVersion.partialVersion(scalaVersion.value).exists(_._1 != 2)
}

val sharedSettings = Seq(
  libraryDependencies ++= Seq(
    "org.typelevel"     %%% "simulacrum-scalafix-annotations" % simulacrumScalafixVersion,
    "org.scalatest"     %%% "scalatest"                       % "3.2.19"   % Test,
    "org.scalatestplus" %%% "scalacheck-1-16"                 % "3.2.14.0" % Test,
    "org.scalacheck"    %%% "scalacheck"                      % "1.18.1"   % Test,
    "org.typelevel"     %%% "cats-laws"                       % "2.12.0"   % Test
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
    "org.typelevel" %%% "cats-core"  % "2.12.0",
    "org.typelevel" %%% "cats-parse" % "1.0.0"
  ),
  libraryDependencies ++= (if (isScala3.value) Nil else Seq("com.chuusai" %%% "shapeless" % "2.3.12")),
  pomPostProcess := { node =>
    new RuleTransformer(new RewriteRule {
      override def transform(node: xml.Node): Seq[xml.Node] = {
        removePomDependency(groupId = "org.typelevel", artifactIdPrefix = "simulacrum")
          .applyOrElse(node, (_: xml.Node) => Seq(node))
      }
    }).transform(node).head
  }
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

lazy val updatePublicSuffixes =
  taskKey[Unit]("Updates the public suffix Set at io.lemonlabs.uri.internet.PublicSuffixes")

updatePublicSuffixes := UpdatePublicSuffixes.generate()

addCommandAlias("check", ";scalafmtCheckAll;scalafmtSbtCheck")
addCommandAlias("fmt", ";scalafmtAll;scalafmtSbt")
