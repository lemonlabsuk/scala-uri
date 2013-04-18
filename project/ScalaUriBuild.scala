import sbt._
import sbt.Keys._

object ScalaUriBuild extends Build {

  lazy val scalaUri = Project(
    id = "scala-uri",
    base = file(".")
  )
}