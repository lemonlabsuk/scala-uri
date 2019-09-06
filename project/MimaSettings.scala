import com.typesafe.tools.mima.core.{DirectMissingMethodProblem, ProblemFilters, ReversedMissingMethodProblem}
import com.typesafe.tools.mima.plugin.MimaKeys.{mimaBinaryIssueFilters, mimaPreviousArtifacts, mimaReportBinaryIssues}
import com.typesafe.tools.mima.plugin.MimaPlugin
import sbt.Keys._
import sbt._

object MimaSettings {

  val previousVersions = (0 to 1).map(patch => s"1.5.$patch").toSet

  val mimaExcludes = Seq(
    ProblemFilters.exclude[ReversedMissingMethodProblem]("io.lemonlabs.uri.typesafe.QueryValueInstances1.*")
  )

  val mimaSettings = MimaPlugin.mimaDefaultSettings ++ Seq(
    mimaPreviousArtifacts := {
      CrossVersion.partialVersion(scalaVersion.value) match {
        case Some((2, scalaMajor)) if scalaMajor <= 13 =>
          previousVersions.map { organization.value % s"${name.value}_${scalaBinaryVersion.value}" % _ }
        case _ => Set.empty
      }
    },
    mimaBinaryIssueFilters ++= {
      CrossVersion.partialVersion(scalaVersion.value) match {
        case Some((2, 12)) =>
          mimaExcludes ++ Seq(
            // In scala 2.12 adding a method to a value class breaks binary compatibility (see here: ).
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
