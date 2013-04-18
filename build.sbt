name := "scala-uri"

organization  := "com.github.theon"

version       := "0.3.5"

scalaVersion  := "2.10.0"

crossScalaVersions := Seq("2.9.2", "2.10.0")

publishMavenStyle := true

publishArtifact in Test := false

pomIncludeRepository := { _ => false }

scalacOptions := Seq("-unchecked", "-deprecation", "-encoding", "utf8")

libraryDependencies += "org.parboiled" %% "parboiled-scala" % "1.1.4"

libraryDependencies <+= scalaVersion(scalatestDependency(_))

seq(ScctPlugin.instrumentSettings : _*)

parallelExecution in Test := false

publishTo <<= version { (v: String) =>
  val nexus = "https://oss.sonatype.org/"
  if (v.trim.endsWith("SNAPSHOT"))
    Some("snapshots" at nexus + "content/repositories/snapshots")
  else
    Some("releases"  at nexus + "service/local/staging/deploy/maven2")
}

pomExtra := (
  <url>https://github.com/theon/scala-uri</url>
  <licenses>
    <license>
      <name>Apache 2</name>
      <url>http://www.apache.org/licenses/LICENSE-2.0</url>
      <distribution>repo</distribution>
    </license>
  </licenses>
  <scm>
    <url>git@github.com:theon/scala-uri.git</url>
    <connection>scm:git@github.com:theon/scala-uri.git</connection>
  </scm>
  <developers>
    <developer>
      <id>theon</id>
      <name>Ian Forsey</name>
      <url>http://theon.github.com</url>
    </developer>
  </developers>)

resolvers += "Sonatype OSS Snapshots" at
  "https://oss.sonatype.org/content/repositories/snapshots"

libraryDependencies += "com.github.axel22" %% "scalameter" % "0.3"

testFrameworks += new TestFramework("benchmarks.OnOffScalaMeterFramework")

logBuffered := false

//The `-Cbenchmarking false` prevents the Performance Benchmarks running for the sbt command `test` and `scct:test`
//See the benchmark script in the root of the repo for examples of how to run the Performance Benchmarks
testOptions in Test += Tests.Argument(new TestFramework("benchmarks.OnOffScalaMeterFramework"), "-Cbenchmarking false")