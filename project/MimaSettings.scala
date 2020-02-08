import com.typesafe.tools.mima.core.{DirectMissingMethodProblem, ProblemFilters, ReversedMissingMethodProblem}
import com.typesafe.tools.mima.plugin.MimaKeys.{mimaBinaryIssueFilters, mimaPreviousArtifacts, mimaReportBinaryIssues}
import com.typesafe.tools.mima.plugin.MimaPlugin
import sbt.Keys._
import sbt._

object MimaSettings {
  val previousVersions = (0 to 0).map(v => s"2.$v.0").toSet

  val mimaExcludes = Seq(
    ProblemFilters.exclude[ReversedMissingMethodProblem]("io.lemonlabs.uri.typesafe.QueryValueInstances1.*")
  )

  val mimaSettings = MimaPlugin.mimaDefaultSettings ++ Seq(
    mimaPreviousArtifacts := {
      if (VersionNumber(scalaVersion.value).matchesSemVer(SemanticSelector("<=2.13")))
        previousVersions.map { organization.value % s"${name.value}_${scalaBinaryVersion.value}" % _ } else
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
    test in Test := {
      mimaReportBinaryIssues.value
      mimaBinaryIssueFilters.value
      (test in Test).value
    }
  )
}
