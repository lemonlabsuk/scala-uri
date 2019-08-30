import com.typesafe.tools.mima.core.{ProblemFilters, ReversedMissingMethodProblem}
import com.typesafe.tools.mima.plugin.MimaKeys.{mimaPreviousArtifacts, mimaReportBinaryIssues, mimaBinaryIssueFilters}
import com.typesafe.tools.mima.plugin.MimaPlugin
import sbt.Keys._
import sbt._

object MimaSettings {

  val previousVersions = (0 to 1).map(patch => s"1.5.$patch").toSet

  val mimaSettings = MimaPlugin.mimaDefaultSettings ++ Seq(
    mimaPreviousArtifacts := {
      CrossVersion.partialVersion(scalaVersion.value) match {
        case Some((2, scalaMajor)) if scalaMajor <= 13 =>
          previousVersions.map { organization.value % s"${name.value}_${scalaBinaryVersion.value}" % _ }
        case _ => Set.empty
      }
    },
    mimaBinaryIssueFilters ++= Seq(
      ProblemFilters.exclude[ReversedMissingMethodProblem]("io.lemonlabs.uri.typesafe.QueryValueInstances1.*")
    ),
    test in Test := {
      mimaReportBinaryIssues.value
      mimaBinaryIssueFilters.value
      (test in Test).value
    }
  )
}
