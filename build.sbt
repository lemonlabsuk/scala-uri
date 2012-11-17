organization  := "com.github.theon"

version       := "0.1-SNAPSHOT"

scalaVersion  := "2.9.2"

scalacOptions := Seq("-Ydependent-method-types", "-unchecked", "-deprecation", "-encoding", "utf8")

resolvers ++= Seq(
    "Sonatype OSS" at "http://oss.sonatype.org/content/groups/scala-tools"
)

libraryDependencies ++= Seq(
  "org.scalatest" %% "scalatest" % "1.8" % "test"
)