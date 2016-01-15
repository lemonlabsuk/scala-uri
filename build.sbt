import scoverage.ScoverageSbtPlugin.ScoverageKeys._

name := "scala-uri"

organization  := "com.netaporter"

version       := "0.4.12-SNAPSHOT"

scalaVersion  := "2.11.7"

crossScalaVersions := Seq("2.10.5", "2.11.7")

def coverageEnabled(scalaVersion: String) = scalaVersion match {
  case v if v startsWith "2.10" => false
  case _ => true
}

lazy val updatePublicSuffixes = taskKey[Unit]("Updates the public suffix Trie at com.netaporter.uri.internet.PublicSuffixes")

updatePublicSuffixes := UpdatePublicSuffixTrie.generate()

coverageOutputXML := coverageEnabled(scalaVersion.value)

coverageOutputCobertua := coverageEnabled(scalaVersion.value)

coverageHighlighting := coverageEnabled(scalaVersion.value)

publishMavenStyle := true

publishArtifact in Test := false

pomIncludeRepository := { _ => false }

resolvers += "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots"

resolvers += "Sonatype OSS Releases" at "https://oss.sonatype.org/content/repositories/releases"

scalacOptions := Seq("-unchecked", "-deprecation", "-encoding", "utf8", "-feature")

libraryDependencies += "org.parboiled" %% "parboiled" % "2.1.0"

libraryDependencies += "io.spray" %%  "spray-json" % "1.3.2"

libraryDependencies += "org.scalatest" %% "scalatest" % "2.1.4" % "test"

parallelExecution in Test := false

publishTo <<= version { (v: String) =>
  val nexus = "https://oss.sonatype.org/"
  if (v.trim.endsWith("SNAPSHOT"))
    Some("snapshots" at nexus + "content/repositories/snapshots")
  else
    Some("releases"  at nexus + "service/local/staging/deploy/maven2")
}

pomExtra := (
  <url>https://github.com/net-a-porter/scala-uri</url>
  <licenses>
    <license>
      <name>Apache 2</name>
      <url>http://www.apache.org/licenses/LICENSE-2.0</url>
      <distribution>repo</distribution>
    </license>
  </licenses>
  <scm>
    <url>git@github.com:net-a-porter/scala-uri.git</url>
    <connection>scm:git@github.com:net-a-porter/scala-uri.git</connection>
  </scm>
  <developers>
    <developer>
      <id>theon</id>
      <name>Ian Forsey</name>
      <url>http://theon.github.io</url>
    </developer>
  </developers>)
