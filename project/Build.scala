import sbt._
import sbt.Keys._

object ScalaMockBuild extends Build {

  lazy val scalaUri = Project(
    id = "scala-uri",
    base = file(".")
  )

  def scalatestDependency(scalaVersion: String) = {
    val scala292 = """2\.9\.2.*""".r
    val scala210 = """2\.10\.0.*""".r
    scalaVersion match {
      case scala292() => "org.scalatest" % "scalatest_2.9.2" % "2.0.M5" % "test"
      case scala210() => "org.scalatest" % "scalatest_2.10.0-RC5" % "2.0.M5-B1" % "test"
    }
  }
}