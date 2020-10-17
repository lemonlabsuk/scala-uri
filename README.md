# scala-uri

[![Build Status](https://travis-ci.org/lemonlabsuk/scala-uri.svg?branch=master)](https://travis-ci.org/lemonlabsuk/scala-uri)
[![codecov.io](http://codecov.io/github/lemonlabsuk/scala-uri/coverage.svg?branch=master)](https://codecov.io/gh/lemonlabsuk/scala-uri/branch/master)
[![Slack](https://lemonlabs.io/slack/badge.svg)](https://lemonlabs.io/slack)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/io.lemonlabs/scala-uri_2.13/badge.svg)](https://maven-badges.herokuapp.com/maven-central/io.lemonlabs/scala-uri_2.12)
[![Scala.js](https://www.scala-js.org/assets/badges/scalajs-1.0.0.svg)](#scalajs-support)
[![Average time to resolve an issue](http://isitmaintained.com/badge/resolution/lemonlabsuk/scala-uri.svg)](http://isitmaintained.com/project/lemonlabsuk/scala-uri "Average time to resolve an issue")
[![Percentage of issues still open](http://isitmaintained.com/badge/open/lemonlabsuk/scala-uri.svg)](http://isitmaintained.com/project/lemonlabsuk/scala-uri "Percentage of issues still open")

[![Cats Friendly Badge](https://typelevel.org/cats/img/cats-badge-tiny.png)](#cats-support) 

`scala-uri` is a small Scala library that helps you work with URIs. It has the following features:

 * A [RFC 3986](https://www.ietf.org/rfc/rfc3986.txt) compliant [parser](#parsing) to parse URLs and URNs from Strings
 * URL [Builders](#building-urls) to create URLs from scratch
 * Ability to transform query strings with methods such as [filterQuery](#filterquery) and [mapQuery](#mapquery)
 * Ability to [replace](#replacing-query-string-parameters) and [remove](#removing-query-string-parameters) query string parameters
 * Ability to extract TLDs and [public suffixes](#public-suffixes) such as `.com` and `.co.uk` from hosts
 * Ability to render URLs in [punycode](#punycode)
 * Ability to [parse](#parsing-ips) IPv6 and IPv4 addresses
 * Support for [custom encoding](#custom-encoding) such as encoding [spaces as pluses](#encoding-spaces-as-pluses)
 * Support for [protocol relative urls](#protocol-relative-urls)
 * Support for [user information](#user-information) e.g. `ftp://user:password@mysite.com`
 * Support for [URNs](#parse-a-urn)
 * Support for [mailto](#mailto) URLs
 * Support for [data](#data-urls) URLs as defined in [RFC2397](https://tools.ietf.org/html/rfc2397)
 * Support for [git](#gitscp-style-urls) scp-like URLs 
 * Support for [Scala.js](#scalajs-support)
 * Support for [cats](#cats-support)
 * No dependencies on existing web frameworks

To include it in your SBT project from maven central:

```scala
"io.lemonlabs" %% "scala-uri" % "2.3.1"
```

## Migration Guides

 * [2.0.0+](#1xx-to-2xx)
 * [1.5.0+](#1xx-to-15x)
 * [1.0.0+](#05x-to-1xx)
 * [Older versions](#04x-to-05x)

There are also demo projects for both [scala](https://github.com/lemonlabsuk/scala-uri-demo) and [Scala.js](https://github.com/lemonlabsuk/scala-uri-scalajs-example) to help you get up and running quickly.

## Parsing

### Parse a URL

```scala mdoc:reset
import io.lemonlabs.uri.Url

val url = Url.parse("https://www.scala-lang.org")
```

The returned value has type `Url` with an underlying implementation of `AbsoluteUrl`, `RelativeUrl`,
`UrlWithoutAuthority`, `ProtocolRelativeUrl` or `DataUrl`. If you know your URL will always be one of these types, you can
use the following `parse` methods to get a more specific return type

```scala mdoc:reset
import io.lemonlabs.uri._

val absoluteUrl = AbsoluteUrl.parse("https://www.scala-lang.org")
val relativeUrl = RelativeUrl.parse("/index.html")
val mailtoUrl = UrlWithoutAuthority.parse("mailto:test@example.com")
val protocolRelativeUrl = ProtocolRelativeUrl.parse("//www.scala-lang.org")
val dataUrl = DataUrl.parse("data:text/plain;base64,SGVsbG8sIFdvcmxkIQ%3D%3D")
```

**Note:** scala-uri only supports parsing port numbers less than `Int.MaxValue`, deviating from RFC3986 which does
not impose a limit

## Parse a URN

```scala mdoc:reset
import io.lemonlabs.uri.Urn

val urn = Urn.parse("urn:isbn:0981531687")
urn.scheme // This is "urn"
urn.nid // This is "isbn"
urn.nss // This is "0981531687"
```

## Parse a URI

You can use `Uri.parse` to parse URNs as well as URLs. `Url.parse` and `Urn.parse` are preferable as they return
a more specific return type

## Building URLs

`Url` provides an apply method with a bunch of optional parameters that can be used to build URLs

```scala mdoc:reset
import io.lemonlabs.uri.{Url, QueryString}

val url = Url(scheme = "http", host = "lemonlabs.io", path = "/opensource")
val url2 = Url(path = "/opensource", query = QueryString.fromPairs("param1" -> "a", "param2" -> "b"))
```

## Transforming URLs

### mapQuery

The `mapQuery` method will transform the Query String of a URI by applying the specified `PartialFunction` to each
Query String Parameter. Any parameters not matched in the `PartialFunction` will be left as-is.

```scala mdoc:reset
import io.lemonlabs.uri.Url

val uri = Url.parse("/scala-uri?p1=one&p2=2&p3=true")

// Results in /scala-uri?p1_map=one_map&p2_map=2_map&p3_map=true_map
uri.mapQuery {
  case (n, Some(v)) => (n + "_map", Some(v + "_map"))
}
```

The `mapQueryNames` and `mapQueryValues` provide a more convenient way to transform just Query Parameter names or values

```scala mdoc:reset
import io.lemonlabs.uri.Url

val uri = Url.parse("/scala-uri?p1=one&p2=2&p3=true")

uri.mapQueryNames(_.toUpperCase) // Results in /scala-uri?P1_map=one&P2=2&P3=true
uri.mapQueryValues(_.replace("true", "false")) // Results in /scala-uri?p1=one&p2=2&p3=false
```

### filterQuery

The `filterQuery` method will remove any Query String Parameters for which the provided Function returns false

```scala mdoc:reset
import io.lemonlabs.uri.Url

val uri = Url.parse("/scala-uri?p1=one&p2=2&p3=true")

// Results in /scala-uri?p2=2
uri.filterQuery {
  case (n, v) => n.contains("2") && v.contains("2")
}

uri.filterQuery(_._1 == "p1") // Results in /scala-uri?p1=one
```

The `filterQueryNames` and `filterQueryValues` provide a more convenient way to filter just by Query Parameter name or value

```scala mdoc:reset
import io.lemonlabs.uri.Url

val uri = Url.parse("/scala-uri?p1=one&p2=2&p3=true")

uri.filterQueryNames(_ > "p1") // Results in /scala-uri?p2=2&p3=true
uri.filterQueryValues(_.length == 1) // Results in /scala-uri?p2=2
```

### collectQuery

The `collectQuery` method will transform the Query String of a URI by applying the specified `PartialFunction` to each
Query String Parameter. Any parameters not matched in the `PartialFunction` will be removed.

```scala mdoc:reset
import io.lemonlabs.uri.Url

val uri = Url.parse("/scala-uri?p1=one&p2=2&p3=true")

// Results in /scala-uri?p1_map=one_map
uri.collectQuery {
  case ("p1", Some(v)) => ("p1_map", Some(v + "_map"))
}
```

### Convert an Absolute URL to a Relative URL

```scala mdoc:reset
import io.lemonlabs.uri.Url

val absoluteUrl = Url.parse("http://www.example.com/example?a=b")
absoluteUrl.toRelativeUrl // This is /example?a=b
```

### Convert a Relative URL to an Absolute URL

```scala mdoc:reset
import io.lemonlabs.uri.Url

val relativeUrl = Url.parse("/example?a=b")
relativeUrl.withScheme("http").withHost("www.example.com") // This is http://www.example.com/example?a=b
```

## Pattern Matching URIs

```scala mdoc:reset
import io.lemonlabs.uri._

val uri: Uri = Uri.parse("...")
uri match {
    case Uri(path) => // Matches Urns and Urls
    case Urn(path) => // Matches Urns
    case Url(path, query, fragment) => // Matches Urls
    case RelativeUrl(path, query, fragment) => // Matches RelativeUrls
    case UrlWithAuthority(authority, path, query, fragment) => // Matches AbsoluteUrl and ProtocolRelativeUrl
    case AbsoluteUrl(scheme, authority, path, query, fragment) => // Matches AbsoluteUrl
    case ProtocolRelativeUrl(authority, path, query, fragment) => // Matches ProtocolRelativeUrl
    case UrlWithoutAuthority(scheme, path, query, fragment) => // Matches UrlWithoutAuthorityUrl
    case DataUrl(mediaType, base64, data) => // Matches DataUrl
    case ScpLikeUrl(user, host, path) => // Matches ScpLikeUrl
}
```

### Exhaustive matching

In some cases `scalac` will be able to detect instances where not all cases are being matched. For example:

```scala mdoc:reset
import io.lemonlabs.uri._

Uri.parse("/test") match {
  case u: Url => println(u.toString)
}
```

results in the following compiler warning, because `Uri.parse` can return `Urn`s as well as `Url`s:

```
<console>:15: warning: match may not be exhaustive.
It would fail on the following input: Urn(_)
```

In this instance, using `Url.parse` instead of `Uri.parse` would fix this warning


## Hosts

### Parsing Hosts

You can parse a String representing the host part of a URI with `Host.parse`. The return type is `Host` with an
underling implementation of `DomainName`, `IpV4` or `IpV6`.

```scala mdoc:reset
import io.lemonlabs.uri.Host

val host = Host.parse("lemonlabs.io")
```

#### Parsing IPs

```scala mdoc:reset
import io.lemonlabs.uri.{IpV4, IpV6}

val ipv4 = IpV4.parse("13.32.214.142")
val ipv6 = IpV6.parse("[1:2:3:4:5:6:7:8]")
```

### Matching Hosts

```scala mdoc:reset
import io.lemonlabs.uri._

val host: Host = Host.parse("...")
host match {
    case Host(host) => // Matches DomainNames, IpV4s and IpV6s
    case DomainName(host) => // Matches DomainNames
    case ip: IpV4 => // Matches IpV4s
    case ip: IpV6 => // Matches IpV6s
}
```

## Paths

### Matching Paths

```scala mdoc:reset
import io.lemonlabs.uri._

val path: Path = Path.parse("...")
path match {
    case Path(parts) => // Matches any path
    case AbsolutePath(parts) => // Matches any path starting with a slash
    case RootlessPath(parts) => // Matches any path that *doesn't* start with a slash

    case PathParts("a", "b", "c") => // Matches "/a/b/c" and "a/b/c"
    case PathParts("a", "b", _*) => // Matches any path starting with "/a/b" or "a/b"

    case EmptyPath() => // Matches ""
    case PathParts() => // Matches "" and "/"

    case UrnPath("nid", "nss") => // Matches a URN Path "nid:nss"
}
```

## URL Percent Encoding

By Default, `scala-uri` will URL percent encode paths and query string parameters. To prevent this, you can call the `uri.toStringRaw` method:

```scala mdoc:reset
import io.lemonlabs.uri.Url

val uri = Url.parse("http://example.com/path with space?param=üri")

uri.toString // This is: http://example.com/path%20with%20space?param=%C3%BCri

uri.toStringRaw // This is: http://example.com/path with space?param=üri
```

The characters that `scala-uri` will percent encode by default can be found [here](https://github.com/lemonlabsuk/scala-uri/blob/master/shared/src/main/scala/io/lemonlabs/uri/encoding/PercentEncoder.scala#L51). You can modify which characters are percent encoded like so:

Only percent encode the hash character:

```scala mdoc:reset
import io.lemonlabs.uri.config.UriConfig
import io.lemonlabs.uri.encoding._

implicit val config = UriConfig(encoder = percentEncode('#'))
```

Percent encode all the default chars, except the plus character:

```scala mdoc:reset
import io.lemonlabs.uri.config.UriConfig
import io.lemonlabs.uri.encoding._

implicit val config = UriConfig(encoder = percentEncode -- '+')
```

Encode all the default chars, and also encode the letters a and b:

```scala mdoc:reset
import io.lemonlabs.uri.config.UriConfig
import io.lemonlabs.uri.encoding._

implicit val config = UriConfig(encoder = percentEncode ++ ('a', 'b'))
```

### Encoding spaces as pluses

The default behaviour with scala-uri, is to encode spaces as `+` in the querystring and as `%20` elsewhere in the URL.

If you instead wish spaces to be encoded as `%20` in the query, then simply add the following `implicit val` to your code:

```scala mdoc:reset
import io.lemonlabs.uri.Url
import io.lemonlabs.uri.config.UriConfig
import io.lemonlabs.uri.encoding._
import io.lemonlabs.uri.encoding.PercentEncoder._

implicit val config = UriConfig.default.copy(queryEncoder = PercentEncoder())

val uri = Url.parse("http://theon.github.com?test=uri with space")
uri.toString // This is http://theon.github.com?test=uri%20with%20space
```

The default behaviour with scala-uri, is to decode `+` in query string parameters to spaces and to leave it as a literal `+` elsewhere in the URL.

If you instead wish `+` to be left as `+` in the query, then simply add the following `implicit val` to your code:

```scala mdoc:reset
import io.lemonlabs.uri.Url
import io.lemonlabs.uri.config.UriConfig
import io.lemonlabs.uri.decoding._

implicit val config = UriConfig.default.copy(queryDecoder = PercentDecoder)

val uri = Url.parse("http://theon.github.com?test=uri+with+plus")
uri.query.param("test") // This is Some("uri+with+plus")
```

### Custom encoding

If you would like to do some custom encoding for specific characters, you can use the `encodeCharAs` encoder.

```scala mdoc:reset
import io.lemonlabs.uri.Url
import io.lemonlabs.uri.config.UriConfig
import io.lemonlabs.uri.encoding._

implicit val config = UriConfig(encoder = percentEncode + encodeCharAs(' ', "_"))

val uri = Url.parse("http://theon.github.com/uri with space")
uri.toString // This is http://theon.github.com/uri_with_space
```

## URL Percent Decoding

By Default, `scala-uri` will URL percent decode paths and query string parameters during parsing:

```scala mdoc:reset
import io.lemonlabs.uri.Url

val uri = Url.parse("http://example.com/i-have-%25been%25-percent-encoded")

uri.toString // This is: http://example.com/i-have-%25been%25-percent-encoded
uri.toStringRaw // This is: http://example.com/i-have-%been%-percent-encoded
```

To prevent this, you can bring the following implicit into scope:

```scala mdoc:reset
import io.lemonlabs.uri.Url
import io.lemonlabs.uri.config.UriConfig
import io.lemonlabs.uri.decoding.NoopDecoder

implicit val c = UriConfig(decoder = NoopDecoder)

val uri = Url.parse("http://example.com/i-havent-%been%-percent-encoded")

uri.toString // This is: http://example.com/i-havent-%25been%25-percent-encoded
uri.toStringRaw // This is: http://example.com/i-havent-%been%-percent-encoded
```

#### Invalid Percent Encoding

If your Uri contains invalid percent encoding, by default scala-uri will throw a `UriDecodeException`:

```scala crash
Url.parse("/?x=%3") // This throws a UriDecodeException
```

You can configure scala-uri to instead ignore invalid percent encoding and *only* percent decode correctly percent encoded values like so:

```scala mdoc:reset
import io.lemonlabs.uri.Url
import io.lemonlabs.uri.config.UriConfig
import io.lemonlabs.uri.decoding.PercentDecoder

implicit val c = UriConfig(
  decoder = PercentDecoder(ignoreInvalidPercentEncoding = true)
)
val uri = Url.parse("/?x=%3")
uri.toString // This is /?x=%253
uri.toStringRaw // This is /?x=%3
```

## Replacing Query String Parameters

If you wish to replace all existing query string parameters with a given name, you can use the `Url.replaceParams()` method:

```scala mdoc:reset
import io.lemonlabs.uri.Url

val uri = Url.parse("http://example.com/path?param=1")
val newUri = uri.replaceParams("param", "2")

newUri.toString // This is: http://example.com/path?param=2
```

## Removing Query String Parameters

If you wish to remove all existing query string parameters with a given name, you can use the `uri.removeParams()` method:

```scala mdoc:reset
import io.lemonlabs.uri.Url

val uri = Url.parse("http://example.com/path?param=1&param2=2")
val newUri = uri.removeParams("param")

newUri.toString // This is: http://example.com/path?param2=2
```

## Omitting Query Parameters with value `None`

scala-uri has support for not rendering query parameters that have a value of `None`. Set `renderQuery = ExcludeNones`
in your `UriConfig` and make it visible in the scope where you parse/create your `Url`

```scala mdoc:reset
import io.lemonlabs.uri.Url
import io.lemonlabs.uri.config._

implicit val config: UriConfig = UriConfig(renderQuery = ExcludeNones)

val url = Url.parse("http://github.com/lemonlabsuk").addParams("a" -> Some("some"), "b" -> None)
url.toString // This is http://github.com/lemonlabsuk?a=some
```

## Get query string parameters

To get the query string parameters as a `Map[String,Seq[String]]` you can do the following:

```scala mdoc:reset
import io.lemonlabs.uri.Url

val uri = Url.parse("http://example.com/path?a=b&a=c&d=e")
uri.query.paramMap // This is: Map("a" -> Vector("b", "c"), "d" -> Vector("e"))
```

## User Information

`scala-uri` supports user information (username and password) encoded in URLs.

Parsing URLs with user information:

```scala mdoc:reset
import io.lemonlabs.uri.Url

val url = Url.parse("http://user:pass@host.com")
url.user // This is Some("user")
url.password // This is Some("pass")
```

Modifying user information:

```scala mdoc:reset
import io.lemonlabs.uri.AbsoluteUrl

val url = AbsoluteUrl.parse("http://host.com")
url.withUser("jack") // URL is now http://jack@host.com
```

```scala mdoc:reset
import io.lemonlabs.uri.AbsoluteUrl

val url = AbsoluteUrl.parse("http://user:pass@host.com")
url.withPassword("secret") // URL is now http://user:secret@host.com
```

**Note:** that using clear text passwords in URLs is [ill advised](http://tools.ietf.org/html/rfc3986#section-3.2.1)

## Protocol Relative URLs

[Protocol Relative URLs](http://paulirish.com/2010/the-protocol-relative-url/) are supported in `scala-uri`. A `Uri` object with a protocol of `None`, but a host of `Some(x)` will be considered a protocol relative URL.

```scala mdoc:reset
import io.lemonlabs.uri.Url

val uri = Url.parse("//example.com/path") // Return type is Url
uri.schemeOption // This is: None
uri.hostOption // This is: Some("example.com")
```

Use `ProtocolRelativeUrl.parse` if you know your URL will always be Protocol Relative:

```scala mdoc:reset
import io.lemonlabs.uri.ProtocolRelativeUrl

val uri = ProtocolRelativeUrl.parse("//example.com/path") // Return type is ProtocolRelativeUrl
uri.schemeOption // This is: None
uri.host // This is: "example.com"
```

## Character Sets

By default `scala-uri` uses `UTF-8` charset encoding:

```scala mdoc:reset
import io.lemonlabs.uri.Url

val uri = Url.parse("http://theon.github.com/uris-in-scala.html?chinese=网址")
uri.toString // This is http://theon.github.com/uris-in-scala.html?chinese=%E7%BD%91%E5%9D%80
```

This can be changed like so:

```scala mdoc:reset
import io.lemonlabs.uri.config.UriConfig
import io.lemonlabs.uri.Url

implicit val conf = UriConfig(charset = "GB2312")

val uri = Url.parse("http://theon.github.com/uris-in-scala.html?chinese=网址")
uri.toString // This is http://theon.github.com/uris-in-scala.html?chinese=%CD%F8%D6%B7
```

## Subdomains

```scala mdoc:reset
import io.lemonlabs.uri.Url

// This returns Some("www")
Url.parse("http://www.example.com/blah").subdomain

// This returns Some("a.b.c")
Url.parse("http://a.b.c.example.com/blah").subdomain

// This returns None
Url.parse("http://example.com/blah").subdomain

// This returns Vector("a", "a.b", "a.b.c", "a.b.c.example")
Url.parse("http://a.b.c.example.com/blah").subdomains

// This returns Some("a")
Url.parse("http://a.b.c.example.com/blah").shortestSubdomain

// This returns Some("a.b.c.example")
Url.parse("http://a.b.c.example.com/blah").longestSubdomain
```

These methods return `None` or `Vector.empty` for URLs without a Host (e.g. Relative URLs)

## Apex Domains

**Note:** *Currently not supported for Scala.js*

The method `apexDomain` returns the [apex domain](https://help.github.com/articles/about-supported-custom-domains/#apex-domains)
for the URL (e.g. `example.com` for `http://www.example.com/path`)

```scala mdoc:reset
import io.lemonlabs.uri.Url

val uri = Url.parse("http://www.google.co.uk/blah")
uri.apexDomain // This returns Some("google.co.uk")
```

## Public Suffixes

`scala-uri` uses the list of public suffixes from [publicsuffix.org](https://publicsuffix.org) to allow you to identify
the TLD of your absolute URIs.

The `publicSuffix` method returns the longest public suffix from your URI

```scala mdoc:reset
import io.lemonlabs.uri.Url

val uri = Url.parse("http://www.google.co.uk/blah")
uri.publicSuffix // This returns Some("co.uk")
```

The `publicSuffixes` method returns all the public suffixes from your URI

```scala mdoc:reset
import io.lemonlabs.uri.Url

val uri = Url.parse("http://www.google.co.uk/blah")
uri.publicSuffixes // This returns Vector("co.uk", "uk")
```

These methods return `None` and `Vector.empty`, respectively for URLs without a Host (e.g. Relative URLs)

## Punycode

See [RFC 3490](http://www.ietf.org/rfc/rfc3490.txt)

```scala mdoc:reset
import io.lemonlabs.uri.Url

val url = Url.parse("https://はじめよう.みんな/howto.html")
url.toStringPunycode // This returns "https://xn--p8j9a0d9c9a.xn--q9jyb4c/howto.html"
```

## mailto

Mailto URLs are best parsed with `UrlWithoutAuthority.parse`, but can also be parsed with `Url.parse`

```scala mdoc:reset
import io.lemonlabs.uri.UrlWithoutAuthority

val mailto = UrlWithoutAuthority.parse("mailto:someone@example.com?subject=Hello")
mailto.scheme // This is Some(mailto")
mailto.path // This is "someone@example.com"
mailto.query.param("subject") // This is Some("Hello")
```

## Data URLs

Data URLs are defined in [RFC2397](https://tools.ietf.org/html/rfc2397)

### Base64 encoded data URLs

```scala mdoc:reset
import java.io.ByteArrayInputStream
import io.lemonlabs.uri.DataUrl
import javax.imageio.ImageIO

// A data URL containing a PNG image of a red dot
val dataUrl = DataUrl.parse("data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAUAAAAFCAYAAACNbyblAAAAHElEQVQI12P4//8/w38GIAXDIBKE0DHxgljNBAAO9TXL0Y4OHwAAAABJRU5ErkJggg==")

dataUrl.scheme // This is "data"
dataUrl.mediaType.value // This is "image/png"
dataUrl.base64 // This is true

// Convert the image data to a java.awt.image.BufferedImage
val image = ImageIO.read(new ByteArrayInputStream(dataUrl.data))
```

### Percent encoded data URLs

```scala mdoc:reset
import io.lemonlabs.uri.DataUrl

val dataUrl = DataUrl.parse("data:text/plain;charset=UTF-8;page=21,the%20data:1234,5678")

dataUrl.mediaType.value // This is text/plain
dataUrl.mediaType.charset // This is UTF-8
dataUrl.mediaType.parameters // This is Vector("charset" -> "UTF-8", "page" -> "21")
dataUrl.base64 // This is false

dataUrl.dataAsString // This is "the data:1234,5678"
```

## git/scp style URLs 

git/scp style URLs can be parsed like so:

```scala mdoc:reset
import io.lemonlabs.uri.ScpLikeUrl

val url = ScpLikeUrl.parse("git@github.com:lemonlabsuk/scala-uri.git")
url.user // This is Some("git")
url.host.toString // This is "github.com"
url.path.toString // This is "lemonlabsuk/scala-uri.git"
```

Note that `ScpLikeUrl.parse`, should only be used for git URLs with scp-like syntax (with a `:`
between the host and path). For all other git URLs, `AbsoluteUrl.parse` or `Url.parse` should 
be used:

```scala mdoc:reset
import io.lemonlabs.uri.AbsoluteUrl

val gitUrl    = AbsoluteUrl.parse("git://github.com/lemonlabsuk/scala-uri.git")
val gitSshUrl = AbsoluteUrl.parse("git+ssh://git@github.com/lemonlabsuk/scala-uri.git")
val sshUrl    = AbsoluteUrl.parse("ssh://git@github.com/lemonlabsuk/scala-uri.git")
val httpsUrl  = AbsoluteUrl.parse("https://github.com/lemonlabsuk/scala-uri.git")
```

## Typesafe URL builder DSL

The version of DSL which relies on the types to render urls providing better control over
the way values would be translated to url parts.

It is possible to use arbitrary types as parts of the url:  

### Query Strings

```scala mdoc:reset
import io.lemonlabs.uri.typesafe._
import io.lemonlabs.uri.typesafe.dsl._

final case class Foo(a: Int, b: String)

object Foo {
  implicit val traversableParams: TraversableParams[Foo] = TraversableParams.product
}

val uri = "http://theon.github.com/scala-uri" addParams Foo(a = 1, b = "bar")
uri.toString //This is: http://theon.github.com/scala-uri?a=1&b=bar
```

### Query String Values

```scala mdoc:reset
import io.lemonlabs.uri.typesafe._
import io.lemonlabs.uri.typesafe.dsl._

sealed trait Bar {
  def name: String
}

case object A extends Bar {
  val name: String = "A"
}

case object B extends Bar {
  val name: String = "B"
}

object Bar {
  implicit val queryValue: QueryValue[Bar] = QueryValue.derive[Bar].by(_.name)
}

val uri = "http://theon.github.com/scala-uri" ? ("foo" -> A)
uri.toString //This is: http://theon.github.com/scala-uri?foo=A
```

### Path Parts

```scala mdoc:reset
import io.lemonlabs.uri.typesafe._
import io.lemonlabs.uri.typesafe.dsl._

final case class Foo(a: String, b: Int)

object Foo {
  implicit val pathPart: PathPart[Foo] = (foo: Foo) => s"${foo.a}/${foo.b}"
}


val uri = "http://theon.github.com" / "scala-uri" / Foo(a = "user", b = 1)
uri.toString //This is: http://theon.github.com/scala-uri/user/1
```

### Fragments

```scala mdoc:reset
import io.lemonlabs.uri.typesafe._
import io.lemonlabs.uri.typesafe.dsl._

final case class Foo(a: String, b: Int)
object Foo {
  implicit val fragment: Fragment[Foo] = (foo: Foo) => Some(s"${foo.a}-${foo.b}")
}

val uri5 = "http://theon.github.com/scala-uri" `#` Foo(a = "user", b = 1)
uri5.toString //This is: http://theon.github.com/scala-uri#user-1
```

## Scala.js support

See [scala-uri-scalajs-example](https://github.com/lemonlabsuk/scala-uri-scalajs-example) for usage

## Cats Support

scala-uri provides type class instances of `cats.Eq`, `cats.Show` and `cats.Order` for:
`Uri `, `Url`, `RelativeUrl`, `UrlWithAuthority`, `ProtocolRelativeUrl`, `AbsoluteUrl`, 
`UrlWithoutAuthority`, `SimpleUrlWithoutAuthority`, `DataUrl`, `ScpLikeUrl`, `Urn`, `Authority`, `UserInfo`, 
`Host`, `DomainName`, `IpV4`, `IpV6`, `MediaType`, `Path`, `UrlPath`, `AbsoluteOrEmptyPath`, 
`RootlessPath`, `AbsolutePath`, `UrnPath`, `QueryString`

The type class instances exist in the companion objects for these types.

## Including scala-uri your project

`scala-uri` `2.x.x` is currently built with support for Scala `2.13.x`, Scala `2.12.x` and Scala.js `1.1.0+` 

 * For `2.11.x` support use `scala-uri` `1.4.10` from branch [`1.4.x`](https://github.com/lemonlabsuk/scala-uri/tree/1.4.x)
 * For `2.10.x` support use `scala-uri` `0.4.17` from branch [`0.4.x`](https://github.com/lemonlabsuk/scala-uri/tree/0.4.x)
 * For `2.9.x` support use `scala-uri` `0.3.6` from branch [`0.3.x`](https://github.com/lemonlabsuk/scala-uri/tree/0.3.x)
 * For Scala.js `1.x.x` support, use `scala-uri` `2.3.1`
 * For Scala.js `0.6.x` support, use `scala-uri` `2.2.3`

Release builds are available in maven central. For SBT users just add the following dependency:

```scala
"io.lemonlabs" %% "scala-uri" % "2.3.1"
```

For maven users you should use (for 2.13.x):

```xml
<dependency>
    <groupId>io.lemonlabs</groupId>
    <artifactId>scala-uri_2.13</artifactId>
    <version>2.3.1</version>
</dependency>
```

# Contributions

Contributions to `scala-uri` are always welcome. Check out the [Contributing Guidelines](https://github.com/lemonlabsuk/scala-uri/blob/master/README.md)

# Migration guides

## 2.x.x to 3.x.x

 * *Backwards Incompatible*: The space character is now encoded to `+` instead of `%20` in query string parameters by default.   
 * *Backwards Incompatible*: The `+` method in `io.lemonlabs.uri.encoding.UriEncoder`, now chains encoders in the opposite order to be more intuitive.
   E.g. `a + b` will encode with encoder `a` first, followed by encoder `b`
 * *Binary Incompatibility*: The following deprecated classes have now been removed:
    * `io.lemonlabs.uri.inet.PublicSuffixTrie`
    * `io.lemonlabs.uri.inet.Trie`
    * `io.lemonlabs.uri.dsl.*`

## 1.x.x to 2.x.x

 * scala-uri no longer depends on a JSON library.
 * *Binary Incompatibility*: The case class `UrlWithoutAuthority` has been renamed `SimpleUrlWithoutAuthority`.
   There is now a trait called `UrlWithoutAuthority`. This trait has a companion object with `apply`, `unapply` and `parse`
   methods, so it mostly can be used in the same way as the previous case class.
 * *Binary Incompatibility*: Parsing a Data URL will now return an instance of [`DataUrl`](#data-urls) rather than `UrlWithoutAuthority`
 * *Binary Incompatibility*: `UserInfo.user` is now of type `String` rather than `Option[String]`
 * *Binary Incompatibility*: `Authority.userInfo` is now of type `Option[UserInfo]`
 * *Binary Incompatibility*: `UserInfo.empty` method removed
 * *Binary Incompatibility*: `QueryString.fromPairOptions` removed. Use `QueryString.fromPairs` instead.
 * *Binary Incompatibility*: `Url.withQueryStringOptionValues` removed. Use `withQueryString` instead.
 * *Binary Incompatibility*: `Url.addParamsOptionValues` and `QueryString.addParamsOptionValues` have been renames to `addParams`
 * `TypesafeUrlDsl`
    * *Binary Incompatibility*: `withParams[A: TraversableParams](params: A)` renamed to `addParams`
    * `/(PathPart)` no longer splits the part by slash. If you want to add multiple path parts use `/(TraversablePathParts)` instead
 * Type Classes
   * *Binary Incompatibility*: `Fragment[A].fragment` returns `Option[String]` rather than `String`
   * *Binary Incompatibility*: `Url.withFragment` now takes argument of type `T: Fragment` rather than `String` and `Option[String]` 
     Type Class instances are provided the method can be used with `String` and `Option[String]` values just as before
   * *Binary Incompatibility*: `Url.addPathParts` and `Url.addPathPart` now takes arguments of type `P: TraversablePathParts` or `P: PathPart` rather than `Iterable[String]` or `String`
     Type Class instances are provided the methods can be used with `String` and `Iterable[String]` values just as before
   * *Binary Incompatibility*: `Url.withQueryString`, `Url.addParam`, `Url.addParams`, `Url.replaceParams`, `Url.removeParams`, 
     `Url.mapQuery`, `Url.flatMapQuery`, `Url.collectQuery`, `Url.mapQueryNames` and `Url.mapQueryValues` now takes argument of type `KV: QueryKeyValue`, `K: QueryKey` or `V: QueryValue` rather than `(String, String)` or `String`
     Type Class instances are provided the methods can be used with `(String, String)` or `String` values just as before   
 * The [URL builder DSL](#url-builder-dsl) has been deprecated in favour of the [Typesafe URL builder DSL](#typesafe-url-builder-dsl)
 * `Authority.parse` no longer expects it's string argument to start with `//`, as this is not part of the Authority, it is a delimiter. See [RFC 3986](https://www.ietf.org/rfc/rfc3986.txt)
 * `UserInfo.parse` no longer expects it's string argument to end with a `@`, as this is not part of the UserInfo, it is a delimiter. See [RFC 3986](https://www.ietf.org/rfc/rfc3986.txt)
 * `QueryString.toString` no longer returns a leading `?`, as this is not part of the query string, it is a delimiter. See [RFC 3986](https://www.ietf.org/rfc/rfc3986.txt) 
 * Forward slashes in paths are now percent encoded by default.
   This means `Url.parse("/%2F/").toString` returns `"/%2F/"` rather than `///` in previous versions
   To return to the previous behavior, you can bring a `UriConfig` like so into scope
   ```scala mdoc:reset
    import io.lemonlabs.uri.encoding.PercentEncoder._
    implicit val c = UriConfig.default.copy(pathEncoder = PercentEncoder(PATH_CHARS_TO_ENCODE - '/'))
   ```

## 1.x.x to 1.5.x

 * scala 2.11 support dropped, please upgrade to 2.12 or 2.13

## 0.5.x to 1.x.x

Thanks to [@evanbennett](https://github.com/evanbennett). `1.x.x` is inspired by his fork [here](https://github.com/evanbennett/scala-uri)
and discussion [here](https://github.com/NET-A-PORTER/scala-uri/pull/113).

 * Package change from `com.netaporter.uri` to `io.lemonlabs.uri`
 * The single `Uri` case class has now been replaced with a class hierarchy. Use the most specific class in this
   hierarchy that fits your use case
 * `Uri` used to be a case class, but the replacements `Uri` and `Url` are now traits. This means they no longer
   have a `copy` method. Use the `with` methods instead (e.g. `withHost`, `withPath` etc)
 * `host` method on `Url` now has return type `Host` rather than `String`. You may have to change `url.host` to `url.host.toString`
 * `path` method on `Url` now has return type `Path` rather than `String`. You may have to change `url.path` to `url.path.toString`
 * Changed parameter value type from `Any` to `String` in methods `addParam`, `addParams`, `replaceParams`.
   Please now call `.toString` before passing non String types to these methods
 * Changed parameter value type from `Option[Any]` to `Option[String]` in method `replaceAll`.
   Please now call `.toString` before passing non String types to this method
 * Query string parameters with a value of `None` will now be rendered with no equals sign by default (e.g. `?param`).
   Previously some methods (such as `?`, `&`, `\?`, `addParam` and `addParams`) would not render parameters with a value of `None` at all.
   In 1.x.x, this behaviour can be achieved by using the [`renderQuery`](#omitting-query-parameters-with-value-none) config option.
 * In most cases `Url.parse` should be used instead of `Uri.parse`. See all parse methods [here](#parse-a-url)
 * `scheme` is now called `schemeOption` on `Uri`. If you have an instance of `AbsoluteUrl` or `ProtocolRelativeUrl`
   there is still `scheme` method but it returns `String` rather than `Option[String]`
 * `protocol` method has been removed from `Uri`. Use `schemeOption` instead
 * Type changed from `Seq` to `Vector` for:
   * `subdomains`, `publicSuffixes`, `params` return type
   * `removeAll` and `removeParams` argument types
   * `params` field in `QueryString`
   * `paramMap` and `pathParts` fields in `Uri`, now `Url`
 * Methods `addParam` and `addParams`  that took Option arguments are now called `addParamOptionValue` and `addParamsOptionValues`
 * Method `replaceAllParams` has been replaced with `withQueryString` or `withQueryStringOptionValues`
 * Method `removeAllParams` has been replaced with `withQueryString(QueryString.empty)`
 * Method `subdomain` has been removed from the Scala.js version. The implementation was incorrect and did not
   match the JVM version of `subdomain`. Once public suffixes are supported for the Scala.js version, a correct
   implementation of `subdomain` can be added
 * Implicit `UriConfig`s now need to be where your `Uri`s are parsed/constructed, rather than where they are rendered
 * Method `hostParts` has been removed from `Uri`. This method predated `publicSuffix` and `subdomain` which are more
   useful methods for pulling apart a host
 * Field `pathStartsWithSlash` removed from `Uri`. This was only intended to be used internally. You can now instead
   check if `Uri.path` is an instance of `AbsolutePath` to determine if the path will start with slash

## 0.4.x to 0.5.x

 * Matrix parameters have been removed. If you still need this, raise an issue
 * scala 2.10 support dropped, please upgrade to 2.11 or 2.12 to use scala-uri 0.5.x
 * Scala.js support added

## 0.3.x to 0.4.x

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
