# scala-uri 0.4.x

[![Build Status](https://travis-ci.org/lemonlabsuk/scala-uri.svg?branch=master)](https://travis-ci.org/NET-A-PORTER/scala-uri)
[![codecov.io](http://codecov.io/github/lemonlabsuk/scala-uri/coverage.svg?branch=master)](https://codecov.io/gh/lemonlabsuk/scala-uri/branch/master)
[![Slack](https://lemonlabs.io/slack/badge.svg)](https://lemonlabs.io/slack)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/io.lemonlabs/scala-uri_2.12/badge.svg)](https://maven-badges.herokuapp.com/maven-central/io.lemonlabs/scala-uri_2.12)

`scala-uri` is a small Scala library that helps you work with URIs. It has the following features:

 * A [DSL](#building-uris-with-the-dsl) for building URIs
 * A [RFC 3986](https://www.ietf.org/rfc/rfc3986.txt) compliant [parser](#parsing-uris) to parse URIs from Strings
 * No dependencies on existing web frameworks
 * Ability to [replace](#replacing-query-string-parameters) and [remove](#removing-query-string-parameters) query string parameters
 * Support for [custom encoding](#custom-encoding) such as encoding [spaces as pluses](#encoding-spaces-as-pluses)
 * Support for [protocol relative urls](#protocol-relative-urls)
 * Support for [user information](#user-information) e.g. `ftp://user:password@mysite.com`
 * Support for [matrix parameters](#matrix-parameters)
 * Support for extracting TLDs and [public suffixes](#public-suffixes) such as `.com` and `.co.uk` from hosts

To include it in your SBT project from maven central:

```scala
"io.lemonlabs" %% "scala-uri" % "0.4.17"
```

There is also a [demo project](https://github.com/NET-A-PORTER/scala-uri-demo) to help you get up and running quickly, from scratch.

*Note:* This library works best when using Scala `2.11.2+`. Due a bug in older versions of Scala, this library  can result in `StackOverflowException`s for very large URLs when using versions of Scala older than `2.11.2`. [More details](https://github.com/NET-A-PORTER/scala-uri/issues/51#issuecomment-45759462)

## Building URIs with the DSL

### Query Strings

```scala
import com.netaporter.uri.dsl._

val uri = "http://theon.github.com/scala-uri" ? ("p1" -> "one") & ("p2" -> 2) & ("p3" -> true)
uri.toString //This is: http://theon.github.com/scala-uri?p1=one&p2=2&p3=true

val uri2 = "http://theon.github.com/scala-uri" ? ("param1" -> Some("1")) & ("param2" -> None)
uri2.toString //This is: http://theon.github.com/scala-uri?param1=1
```

To add query string parameters, use either the `?` or `&` method and pass a `Tuple2` as an argument. The first value in the Tuple is a name of the query string parameter, the second is the value. If a parameter value is an `Option`, it will only be rendered provided it is not `None`.

#### Adding multiple query parameters

```scala
import com.netaporter.uri.dsl._
val p = ("key", true) :: ("key2", false) :: Nil
val uri = "http://example.com".addParams(p)
uri.toString //This is: http://example.com/?key=true&key2=false
```

### Paths

```scala
import com.netaporter.uri.dsl._

val uri = "http://theon.github.com" / "scala-uri"
uri.toString //This is: http://theon.github.com/scala-uri
```

To add path segments, use the `/` method

### Fragments

To set the fragment, use the `` `#` `` method:

```scala
import com.netaporter.uri.dsl._
val uri = "http://theon.github.com/scala-uri" `#` "fragments"

uri.toString //This is: http://theon.github.com/scala-uri#fragments
```

## Parsing URIs

Provided you have the import `com.netaporter.uri.dsl._`, Strings will be implicitly parsed into `Uri` instances:

```scala
import com.netaporter.uri.dsl._
val uri: Uri = "http://theon.github.com/scala-uri?param1=1&param2=2"
```

However, if you prefer, you can call `Uri.parse()` explicitly:

```scala
import com.netaporter.uri.Uri.parse
val uri = parse("http://theon.github.com/scala-uri?param1=1&param2=2")
```

There also exists a `import com.netaporter.uri.Uri.parseQuery` for instances when you wish to parse a query string, not a full URI.

## Transforming URIs

### map

The `mapQuery` method will transform the Query String of a URI by applying the specified Function to each Query String Parameter

```scala
val uri = "/scala-uri" ? ("p1" -> "one") & ("p2" -> 2) & ("p3" -> true)

//Results in /scala-uri?p1_map=one_map&p2_map=2_map&p3_map=true_map
uri.mapQuery {
  case (n, v) => (n + "_map", v + "_map")
}

uri.mapQuery(_.swap) //Results in /scala-uri?one=p1&2=p2&true=p3
```

The `mapQueryNames` and `mapQueryValues` provide a more convenient way to transform just Query Parameter names or values

```scala
val uri = "/scala-uri" ? ("p1" -> "one") & ("p2" -> 2) & ("p3" -> true)

uri.mapQueryNames(_.toUpperCase) //Results in /scala-uri?P1_map=one&P2=2&P3=true

uri.mapQueryValues(_.replace("true", "false")) //Results in /scala-uri?p1=one&p2=2&p3=false
```

### filter

The `filterQuery` method will remove any Query String Parameters for which the provided Function returns false

```scala
val uri = "/scala-uri" ? ("p1" -> "one") & ("p2" -> 2) & ("p3" -> true)

//Results in /scala-uri?p2=2
uri.filterQuery {
  case (n, v) => n.contains("2") && v.contains("2")
}

uri.filterQuery(_._2 == "one") //Results in /scala-uri?p1=one
```

The `filterQueryNames` and `filterQueryValues` provide a more convenient way to filter just by Query Parameter name or value

```scala
val uri = "/scala-uri" ? ("p1" -> "one") & ("p2" -> 2) & ("p3" -> true)

uri.filterQueryNames(_ > "p1") //Results in /scala-uri?p2=2&p3=true

uri.filterQueryValues(_.length == 1) //Results in /scala-uri?p2=2
```

## URL Percent Encoding

By Default, `scala-uri` will URL percent encode paths and query string parameters. To prevent this, you can call the `uri.toStringRaw` method:

```scala
import com.netaporter.uri.dsl._
val uri = "http://example.com/path with space" ? ("param" -> "üri")

uri.toString //This is: http://example.com/path%20with%20space?param=%C3%BCri

uri.toStringRaw //This is: http://example.com/path with space?param=üri
```

The characters that `scala-uri` will percent encode by default can be found [here](https://github.com/NET-A-PORTER/scala-uri/blob/master/src/main/scala/com/netaporter/uri/encoding/PercentEncoder.scala#L21). You can modify which characters are percent encoded like so:

Only percent encode the hash character:

```scala
import com.netaporter.uri.encoding._
implicit val config = UriConfig(encoder = percentEncode('#'))
```

Percent encode all the default chars, except the plus character:

```scala
import com.netaporter.uri.encoding._
implicit val config = UriConfig(encoder = percentEncode -- '+')
```

Encode all the default chars, and also encode the letters a and b:

```scala
import com.netaporter.uri.encoding._
implicit val config = UriConfig(encoder = percentEncode ++ ('a', 'b'))
```

### Encoding spaces as pluses

The default behaviour with scala uri, is to encode spaces as `%20`, however if you instead wish them to be encoded as the `+` symbol, then simply add the following `implicit val` to your code:

```scala
import com.netaporter.uri.dsl._
import com.netaporter.uri.encoding._
implicit val config = UriConfig(encoder = percentEncode + spaceAsPlus)

val uri: Uri = "http://theon.github.com/uri with space"
uri.toString //This is http://theon.github.com/uri+with+space
```

### Custom encoding

If you would like to do some custom encoding for specific characters, you can use the `encodeCharAs` encoder.

```scala
import com.netaporter.uri.dsl._
import com.netaporter.uri.encoding._
implicit val config = UriConfig(encoder = percentEncode + encodeCharAs(' ', "_"))

val uri: Uri = "http://theon.github.com/uri with space"
uri.toString //This is http://theon.github.com/uri_with_space
```

## URL Percent Decoding

By Default, `scala-uri` will URL percent decode paths and query string parameters during parsing:

```scala
import com.netaporter.uri.dsl._
val uri: Uri = "http://example.com/i-have-%25been%25-percent-encoded"

uri.toString //This is: http://example.com/i-have-%25been%25-percent-encoded

uri.toStringRaw //This is: http://example.com/i-have-%been%-percent-encoded
```

To prevent this, you can bring the following implicit into scope:

```scala
import com.netaporter.uri.dsl._
implicit val c = UriConfig(decoder = NoopDecoder)
val uri: Uri = "http://example.com/i-havent-%been%-percent-encoded"

uri.toString // This is: http://example.com/i-havent-%25been%25-percent-encoded

uri.toStringRaw // This is: http://example.com/i-havent-%been%-percent-encoded
```

#### Invalid Percent Encoding

If your Uri contains invalid percent encoding, by default scala-uri will throw a `UriDecodeException`:

```scala
Uri.parse("/?x=%3") // This throws a UriDecodeException
```

You can configure scala-uri to instead ignore invalid percent encoding and *only* percent decode correctly percent encoded values like so:

```scala
implicit val c = UriConfig(
  decoder = PercentDecoder(ignoreInvalidPercentEncoding = true)
)
val uri = Uri.parse("/?x=%3")
uri.toString // This is /?x=%253
uri.toStringRaw // This is /?x=%3
```

## Replacing Query String Parameters

If you wish to replace all existing query string parameters with a given name, you can use the `uri.replaceParams()` method:

```scala
import com.netaporter.uri.dsl._
val uri = "http://example.com/path" ? ("param" -> "1")
val newUri = uri.replaceParams("param", "2")

newUri.toString //This is: http://example.com/path?param=2
```

## Removing Query String Parameters

If you wish to remove all existing query string parameters with a given name, you can use the `uri.removeParams()` method:

```scala
import com.netaporter.uri.dsl._
val uri = "http://example.com/path" ? ("param" -> "1") & ("param2" -> "2")
val newUri = uri.removeParams("param")

newUri.toString //This is: http://example.com/path?param2=2
```

## Get query string parameters

To get the query string parameters as a `Map[String,Seq[String]]` you can do the following:

```scala
import com.netaporter.uri.Uri
val uri = Uri.parse("http://example.com/path?a=b&a=c&d=e")
uri.query.paramMap //This is: Map("a" -> Seq("b", "c"), "d" -> Seq("e"))
```

## User Information

`scala-uri` supports user information (username and password) encoded in URLs.

Parsing URLs with user information:

```scala
val uri = "http://user:pass@host.com"
uri.user //This is Some("user")
uri.password //This is Some("pass")
```

Modifying user information:

```scala
import com.netaporter.uri.dsl._
val mailto = "mailto://user@host.com"
mailto.withUser("jack") //URL is now jack@host.com
```

```scala
import com.netaporter.uri.dsl._
val uri = "http://user:pass@host.com"
uri.withPassword("secret") //URL is now http://user:secret@host.com
```

**Note:** that using clear text passwords in URLs is [ill advised](http://tools.ietf.org/html/rfc3986#section-3.2.1)

## Protocol Relative URLs

[Protocol Relative URLs](http://paulirish.com/2010/the-protocol-relative-url/) are supported in `scala-uri`. A `Uri` object with a protocol of `None`, but a host of `Some(x)` will be considered a protocol relative URL.

```scala
import com.netaporter.uri.dsl._
val uri: Uri = "//example.com/path"
uri.scheme //This is: None
uri.host //This is: Some("example.com")
```

## Matrix Parameters

[Matrix Parameters](http://www.w3.org/DesignIssues/MatrixURIs.html) are supported in `scala-uri`. Support is enabled
using a`UriConfig` with `matrixParams = true` like so:

```scala
import com.netaporter.uri.dsl._

implicit val config = UriConfig(matrixParams = true)
val uri = "http://example.com/path;paramOne=value;paramTwo=value2/pathTwo;paramThree=value3"

//Get parameters at the end of the path
uri.matrixParams //This is Vector("paramThree" -> "value3")

//Add parameters to end of path
val uri2 = uri.addMatrixParam("paramFour", "value4")
uri2.toString //This is http://example.com/path;paramOne=value;paramTwo=value2/pathTwo;paramThree=value3;paramFour=value4

//Get parameters for mid path segment
uri.pathPart("pathTwo").params //This is Vector("paramOne" -> "value", "paramTwo" -> "value2")

//Add parameters for mid path segment
val uri3 = uri.addMatrixParam("pathTwo", "paramFour", "value4")
```

## Character Sets

By default `scala-uri` uses `UTF-8` charset encoding:

```scala
val uri = "http://theon.github.com/uris-in-scala.html" ? ("chinese" -> "网址")
uri.toString //This is http://theon.github.com/uris-in-scala.html?chinese=%E7%BD%91%E5%9D%80
```

This can be changed like so:

```scala
implicit val conf = UriConfig(charset = "GB2312")
val uri = "http://theon.github.com/uris-in-scala.html" ? ("chinese" -> "网址")
uri.toString //This is http://theon.github.com/uris-in-scala.html?chinese=%CD%F8%D6%B7
```

## Public Suffixes

`scala-uri` uses the list of public suffixes from [publicsuffix.org](https://publicsuffix.org) to allow you to identify
the TLD of your absolute URIs.

The `publicSuffix` method returns the longest public suffix from your URI

```scala
val uri = Uri.parse("http://www.google.co.uk/blah")
uri.publicSuffix == Some("co.uk")
```

The `publicSuffixes` method returns all the public suffixes from your URI

```scala
val uri = Uri.parse("http://www.google.co.uk/blah")
uri.publicSuffixes == Seq("co.uk", "uk")
```

These methods return `None` and `Seq.empty`, respectively for relative URIs

## Including scala-uri your project

`scala-uri` `0.4.x` is currently built with support for scala `2.12.x`, `2.11.x` and `2.10.x`

For `2.9.x` support use `scala-uri` [`0.3.x`](https://github.com/net-a-porter/scala-uri/tree/0.3.x)

Release builds are available in maven central. For SBT users just add the following dependency:

```scala
"io.lemonlabs" %% "scala-uri" % "0.4.17"
```

For maven users you should use (for 2.12.x):

```xml
<dependency>
    <groupId>io.lemonlabs</groupId>
    <artifactId>scala-uri_2.12</artifactId>
    <version>0.4.17</version>
</dependency>
```

### Latest snapshot builds

For the latest snapshot builds, add the Sonatype OSS repo to your SBT build configuration:

```scala
resolvers += "Sonatype OSS" at "https://oss.sonatype.org/content/repositories/snapshots"
```

Add the following dependency:

```scala
"io.lemonlabs" %% "scala-uri" % "0.4.18-SNAPSHOT"
```

# Contributions

Contributions to `scala-uri` are always welcome. Good ways to contribute include:

 * Raising bugs and feature requests
 * Fixing bugs and developing new features (I will attempt to merge in pull requests ASAP)
 * Improving the performance of `scala-uri`. See the [Performance Tests](https://github.com/net-a-porter/scala-uri-benchmarks) project for details of how to run the `scala-uri` performance benchmarks.

# Building scala-uri

## Unit Tests

The unit tests can be run from the sbt console by running the `test` command! Checking the unit tests all pass before sending pull requests will be much appreciated.

Generate code coverage reports from the sbt console by running the `scct:test` command. The HTML reports should be generated at `target/scala-2.10/coverage-report/index.html`. Ideally pull requests shouldn't significantly decrease code coverage, but it's not the end of the world if they do. Contributions with no tests are better than no contributions :)

## Performance Tests

For the `scala-uri` performance tests head to the [scala-uri-benchmarks](https://github.com/net-a-porter/scala-uri-benchmarks) github project

# Migration guide from 0.3.x

 * Package changes / import changes
  * All code moved from `com.github.theon` package to `com.netaporter` package
  * `scala-uri` has been organised into the following packages: `encoding`, `decoding`, `config` and `dsl`. You will need to update import statments.
 * Name changes
  * `PermissiveDecoder` renamed to `PermissivePercentDecoder`
  * `QueryString` and `MatrixParams` constructor argument `parameters` shortened to `params`
  * `Uri.parseUri` renamed to `Uri.parse`
  * `protocol` constructor arg in `Uri` renamed to `scheme`
  * `Querystring` renamed to `QueryString`
 * Query String constructor argument `parameters` changed type from `Map[String, List[String]]` to `Seq[(String,String)]`
 * `Uri` constructor argument `pathParts` changed type from `List` to `Vector`
 * `Uri` method to add query string parameters renamed from `params` to `addParams`. Same with `matrixParams` -> `addMatrixParams`
 * `PercentEncoderDefaults` object renamed to `PercentEncoder` companion object.
 * Copy methods `user`/`password`/`port`/`host`/`scheme` now all prefixed with `with`, e.g. `withHost`
 * New `UriConfig` case class used to specify encoders, decoders and charset to be used. See examples in [Custom encoding](#custom-encoding), [URL Percent Decoding](#url-percent-decoding) and [Character Sets](#character-sets)

# License

`scala-uri` is open source software released under the [Apache 2 License](http://www.apache.org/licenses/LICENSE-2.0).
