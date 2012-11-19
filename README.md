# scala-uri

[![Build Status](https://secure.travis-ci.org/theon/scala-uri.png?branch=master)](https://travis-ci.org/theon/scala-uri)

`scala-uri` is a small self contained Scala library that helps you work with URIs. It has a DSL for building URIs and a parser to parse URIs in Strings. It can be used outside a servlet environment as it has zero dependencies on the servlet spec or existing web frameworks.

## Including scala-uri your SBT project

Add the following dependency:

    "com.github.theon" %% "scala-uri" % "0.1"

### Latest snapshot builds

Add the Sonatype OSS repo to your SBT build configuration:

    resolvers += "Sonatype OSS" at "http://oss.sonatype.org/content/public"

Add the following dependency:

    "com.github.theon" %% "scala-uri" % "0.2-SNAPSHOT"

## Building URIs with the DSL

By importing `com.github.theon.uri.Uri._`, Strings can be implicitly converted to URIs. To add query string parameters, use either the `?` or `&` method and pass a `Tuple2` as an argument. The first value in the Tuple is a name of the query string parameter, the second is the value. If a parameter value is an `Option`, it will only be rendered provided it is not `None`.

    import com.github.theon.uri.Uri._
    val uri = "http://theon.github.com/scala-uri" ? ("param1" -> "1") & ("param2" -> Some("2")) & ("param3" -> None)

    uri.toString //Prints http://theon.github.com/scala-uri?param1=1&param2=2

To explicitly create a `Uri`, you can use the following constructors:

    val uri = Uri("http", "theon.github.com", "/scala-uri") //Absolute URI
    val uri = Uri("/scala-uri") //Relative URI

## Parsing URIs

You can parse URIs as Strings into a `Uri` instance like so:

    import com.github.theon.uri.Uri._
    val uri = parseUri("http://theon.github.com/scala-uri?param1=1&param2=2")

# License

`scala-uri` is open source software released under the [Apache 2 License](http://www.apache.org/licenses/LICENSE-2.0).