import com.typesafe.tools.mima.core.{DirectMissingMethodProblem, ProblemFilters, ReversedMissingMethodProblem}
import com.typesafe.tools.mima.plugin.MimaKeys.{mimaBinaryIssueFilters, mimaPreviousArtifacts, mimaReportBinaryIssues}
import com.typesafe.tools.mima.plugin.MimaPlugin
import sbt.Keys._
import sbt._

object MimaSettings {
  
  //val previousVersions = (0 to 0).map(patch => s"2.0.$patch").toSet
  val previousVersions = (1 to 1).map(milestone => s"2.0.0-M$milestone").toSet

  val mimaSettings = MimaPlugin.mimaDefaultSettings ++ Seq(
    mimaPreviousArtifacts := {
      CrossVersion.partialVersion(scalaVersion.value) match {
        case Some((2, scalaMajor)) if scalaMajor <= 13 =>
          previousVersions.map { organization.value % s"${name.value}_${scalaBinaryVersion.value}" % _ }
        case _ => Set.empty
      }
    },
    mimaBinaryIssueFilters ++= Seq(
      ProblemFilters.exclude[ReversedMissingMethodProblem]("io.lemonlabs.uri.typesafe.QueryValueInstances1.*"),
      ProblemFilters.exclude[DirectMissingMethodProblem]("io.lemonlabs.uri.typesafe.dsl.TypesafeUrlDsl.*")
    ),
    test in Test := {
      mimaReportBinaryIssues.value
      mimaBinaryIssueFilters.value
      (test in Test).value
    }
  )
}
