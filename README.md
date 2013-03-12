# scala-uri

[![Build Status](https://secure.travis-ci.org/theon/scala-uri.png?branch=master)](https://travis-ci.org/theon/scala-uri)
[![Coverage Status](https://coveralls.io/repos/theon/scala-uri/badge.png?branch=master)](https://coveralls.io/r/theon/scala-uri)

`scala-uri` is a small Scala library that helps you work with URIs. It has the following features:

 * A [DSL](#building-uris-with-the-dsl) for building URIs
 * A [parser](#parsing-uris) to parse URIs from Strings.
 * Can be used outside a servlet environment as it has zero dependencies on the servlet spec or existing web frameworks.
 * Ability to [replacing](#replacing-query-string-parameters) and [removing](#removing-query-string-parameters) query string parameters
 * Support for [custom encoding](#custom-encoding) such as encoding [spaces as pluses](#encoding-spaces-as-pluses)
 * Support for [protocol relative urls](#protocol-relative-urls)

To include it in your SBT project from maven central:

```scala
"com.github.theon" %% "scala-uri" % "0.3.4"
```

## Building URIs with the DSL

```scala
import com.github.theon.uri.Uri._
val uri = "http://theon.github.com/scala-uri" ? ("p1" -> "one") & ("p2" -> 2) & ("p3" -> true)

uri.toString //This is: http://theon.github.com/scala-uri?p1=one&p2=2&p3=true
```

By importing `com.github.theon.uri.Uri._`, Strings can be _implicitly_ converted to URIs.

To add query string parameters, use either the `?` or `&` method and pass a `Tuple2` as an argument. The first value in the Tuple is a name of the query string parameter, the second is the value. If a parameter value is an `Option`, it will only be rendered provided it is not `None`.

To _explicitly_ create a `Uri`, you can use the following constructors:

```scala
val uri = Uri("http", "theon.github.com", "/scala-uri") //Absolute URI
val uri = Uri("/scala-uri") //Relative URI
```

## Parsing URIs

Provided you have the import `com.github.theon.uri.Uri._`, Strings will be implicitly parsed into `Uri` instances:

```scala
import com.github.theon.uri.Uri._
val uri:Uri = "http://theon.github.com/scala-uri?param1=1&param2=2"
```

However, if you prefer, you can call `parseUri()` explicitly:

```scala
import com.github.theon.uri.Uri.parseUri
val uri = parseUri("http://theon.github.com/scala-uri?param1=1&param2=2")
```

## Options and Query String Parameters

You can specify an `Option` as the value of a query string parameter. It will get rendered if it is `Some(x)` and won't get rendered if it is `None`

```scala
import com.github.theon.uri.Uri._
val uri = "http://theon.github.com/scala-uri" ? ("param1" -> Some("1")) & ("param2" -> None)

uri.toString //This is: http://theon.github.com/scala-uri?param1=1
```

## URL Percent Encoding

By Default, `scala-uri` will URL percent encode paths and query string parameters. To prevent this, you can call the `uri.toStringRaw` method:

```scala
import com.github.theon.uri.Uri._
val uri = "http://example.com/path with space" ? ("param" -> "üri")

uri.toString //This is: http://example.com/path%20with%20space?param=%C3%BCri

uri.toStringRaw //This is: http://example.com/path with space?param=üri
```

### Encoding spaces as pluses

The default behaviour with scala uri, is to encode spaces as `%20`, however if you instead wish them to be encoded as the `+` symbol, then simply add the following `implicit val` to your code:

```scala
import com.github.theon.uri.Uri._
import com.github.theon.uri.Encoders._
implicit val encoder = PercentEncoder + EncodeSpaceAsPlus

val uri:Uri = "http://theon.github.com/uri with space"
uri.toString //This is http://theon.github.com/uri+with+space
```

### Custom encoding

If you would like to do some custom encoding for specific characters, you can use the `EncodeCharAs` encoder.

```scala
import com.github.theon.uri.Uri._
import com.github.theon.uri.Encoders._
implicit val encoder = PercentEncoder + EncodeCharAs(' ', "_")

val uri:Uri = "http://theon.github.com/uri with space"
uri.toString //This is http://theon.github.com/uri_with_space
```


## Replacing Query String Parameters

If you wish to replace all existing query string parameters with a given name, you can use the `uri.replaceParams()` method:

```scala
import com.github.theon.uri.Uri._
val uri = "http://example.com/path" ? ("param" -> "1")
val newUri = uri.replaceParams("param", "2")

newUri.toString //This is: http://example.com/path?param=2
```

## Removing Query String Parameters

If you wish to remove all existing query string parameters with a given name, you can use the `uri.removeParams()` method:

```scala
import com.github.theon.uri.Uri._
val uri = "http://example.com/path" ? ("param" -> "1") & ("param2" -> "2")
val newUri = uri.removeParams("param")

newUri.toString //This is: http://example.com/path?param2=2
```

## Get query string parameters

To get the query string parameters as a `Map[String,List[String]]` you can do the following:

```scala
import com.github.theon.uri.Uri._
val uri = "http://example.com/path" ? ("param" -> "1") & ("param2" -> 2)
uri.query.params //This is: Map(param -> List(1), param2 -> List(2))
```

## Protocol Relative URLs

**New feature currently in 0.3.5-SNAPSHOT**

[Protocol Relative URLs](http://paulirish.com/2010/the-protocol-relative-url/) are supported in `scala-uri`. A `Uri` object with a protocol of `None`, but a host of `Some(x)` will be considered a protocol relative URL.

```scala
import com.github.theon.uri.Uri._
val uri:Uri = "//example.com/path"
uri.scheme //This is: None
uri.host //This is: Some("example.com")
```

## Including scala-uri your SBT project

scala-uri is currently built with support for scala `2.9.2` and `2.10.0`

Release builds are available in maven central. Just add the following dependency:

```scala
"com.github.theon" %% "scala-uri" % "0.3.4"
```

### Latest snapshot builds

For the latest snapshot builds, add the Sonatype OSS repo to your SBT build configuration:

```scala
resolvers += "Sonatype OSS" at "http://oss.sonatype.org/content/public"
```

Add the following dependency:

```scala
"com.github.theon" %% "scala-uri" % "0.3.5-SNAPSHOT"
```

# License

`scala-uri` is open source software released under the [Apache 2 License](http://www.apache.org/licenses/LICENSE-2.0).