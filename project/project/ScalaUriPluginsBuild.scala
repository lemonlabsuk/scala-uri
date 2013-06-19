//import sbt._
//import sbt.Keys._
//
//object ScalaUriPluginsBuild extends Build {
//
//  /**
//   * Used when testing changes to the xsbt-coveralls-plugin
//   * Uncomment this, and comment out
//   *
//   * addSbtPlugin("com.github.theon" %% "xsbt-coveralls-plugin" % "x.x.x")
//   *
//   * in scala-uri/project/build.sbt
//   */
//  lazy val scalaUriRoot = Project(
//    id = "scala-uri",
//    base = file(".")
//  ) dependsOn(coverallsPlugin)
//
//  lazy val coverallsPlugin = file("../../xsbt-coveralls-plugin")
//}