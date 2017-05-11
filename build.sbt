name := "scala-uri"

organization  := "io.lemonlabs"

version       := "0.4.16"

scalaVersion  := "2.12.0"

crossScalaVersions := Seq("2.10.6", "2.11.8", "2.12.0")

coverageEnabled := scalaVersion.value startsWith "2.12"

lazy val updatePublicSuffixes = taskKey[Unit]("Updates the public suffix Trie at com.netaporter.uri.internet.PublicSuffixes")

updatePublicSuffixes := UpdatePublicSuffixTrie.generate()

publishMavenStyle := true

publishArtifact in Test := false

pomIncludeRepository := { _ => false }

resolvers += "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots"

resolvers += "Sonatype OSS Releases" at "https://oss.sonatype.org/content/repositories/releases"

scalacOptions := Seq("-unchecked", "-deprecation", "-encoding", "utf8", "-feature")

libraryDependencies += "org.parboiled" %% "parboiled" % "2.1.3"

libraryDependencies += "io.spray" %%  "spray-json" % "1.3.2"

libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.0" % "test"

parallelExecution in Test := false

publishTo := {
  val nexus = "https://oss.sonatype.org/"
  if (version.value.trim.endsWith("SNAPSHOT"))
    Some("snapshots" at nexus + "content/repositories/snapshots")
  else
    Some("releases"  at nexus + "service/local/staging/deploy/maven2")
}

pomExtra :=
  <url>https://github.com/lemonlabsuk/scala-uri</url>
  <licenses>
    <license>
      <name>Apache 2</name>
      <url>http://www.apache.org/licenses/LICENSE-2.0</url>
      <distribution>repo</distribution>
    </license>
  </licenses>
  <scm>
    <url>git@github.com:lemonlabsuk/scala-uri.git</url>
    <connection>scm:git@github.com:lemonlabsuk/scala-uri.git</connection>
  </scm>
  <developers>
    <developer>
      <id>theon</id>
      <name>Ian Forsey</name>
      <url>http://theon.github.io</url>
    </developer>
  </developers>
