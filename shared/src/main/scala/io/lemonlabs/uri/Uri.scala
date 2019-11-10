package io.lemonlabs.uri

import java.util.Base64

import cats.implicits._
import cats.{Eq, Order, Show}
import io.lemonlabs.uri.config.UriConfig
import io.lemonlabs.uri.parsing.{UriParser, UrlParser, UrnParser}

import scala.util.Try

/**
  * Represents a URI. See [[https://www.ietf.org/rfc/rfc3986 RFC 3986]]
  *
  * Can either be a URL or a URN
  *
  * URLs will be one of these forms:
  *
  *  -           Absolute: `http://example.com`
  *  -  Protocol Relative: `//example.com`
  *  -  Without Authority: `mailto:example@example.com`
  *  -      Root Relative: `/index.html?a=b`
  *  -  Rootless Relative: `index.html?a=b`
  *  -  Rootless Relative
  *    (with doc segment): `../index.html?a=b`
  *
  * URNs will be in the form `urn:example:example2`
  *
  */
sealed trait Uri extends Product with Serializable {
  type Self <: Uri
  type SelfWithScheme <: Uri

  private[uri] def self: Self

  implicit def config: UriConfig

  def schemeOption: Option[String]
  def path: Path

  /**
    * Copies this Uri but with the scheme set as the given value.
    *
    * @param scheme the new scheme to set
    * @return a new Uri with the specified scheme
    */
  def withScheme(scheme: String): SelfWithScheme

  def toUrl: Url
  def toUrn: Urn

  /**
    * Converts to a `java.net.URI`
    *
    * This involves a `toString` and `URI.parse` because the specific `java.net.URI`
    * constructors do not deal properly with encoded elements
    *
    * @return a `java.net.URI` matching this `io.lemonlabs.uri.Uri`
    */
  def toJavaURI: java.net.URI =
    new java.net.URI(toString(config))

  /**
    * Returns the path with no encoders taking place (e.g. non ASCII characters will not be percent encoded)
    * @return String containing the raw path for this Uri
    */
  def toStringRaw: String =
    toString(config.withNoEncoding)

  override def toString: String =
    toString(config)

  private[uri] def toString(config: UriConfig): String
}

object Uri {

  def apply(javaUri: java.net.URI): Uri =
    parse(javaUri.toASCIIString)

  def unapply(uri: Uri): Option[Path] =
    Some(uri.path)

  def parseTry(s: CharSequence)(implicit config: UriConfig = UriConfig.default): Try[Uri] =
    Try(s.toString).flatMap(UriParser.parseUri)

  def parseOption(s: CharSequence)(implicit config: UriConfig = UriConfig.default): Option[Uri] =
    parseTry(s).toOption

  def parse(s: CharSequence)(implicit config: UriConfig = UriConfig.default): Uri =
    parseTry(s).get

  import cats.implicits._

  implicit val eqUri: Eq[Uri] = Eq.fromUniversalEquals
  implicit val showUri: Show[Uri] = Show.fromToString
  implicit val orderUri: Order[Uri] = Order.by(_.toString())
}

/**
  * Represents a URL, which will be one of these forms:
  *
  *  -           Absolute: `http://example.com`
  *  -  Protocol Relative: `//example.com`
  *  -  Without Authority: `mailto:example@example.com`
  *  -      Root Relative: `/index.html?a=b`
  *  -  Rootless Relative: `index.html?a=b`
  *  -  Rootless Relative
  *    (with doc segment): `../index.html?a=b`
  */
sealed trait Url extends Uri {
  type Self <: Url
  type SelfWithScheme <: Url
  type SelfWithAuthority <: UrlWithAuthority

  def hostOption: Option[Host]
  def port: Option[Int]

  def user: Option[String]
  def password: Option[String]

  def path: UrlPath
  def query: QueryString
  def fragment: Option[String]

  /**
    * Returns the longest public suffix for the host in this URI. Examples include:
    *  `com`   for `www.example.com`
    *  `co.uk` for `www.example.co.uk`
    *
    * @return the longest public suffix for the host in this URI
    */
  def publicSuffix: Option[String]

  /**
    * Returns all longest public suffixes for the host in this URI. Examples include:
    *  `com` for `www.example.com`
    *  `co.uk` and `uk` for `www.example.co.uk`
    *
    * @return all public suffixes for the host in this URI
    */
  def publicSuffixes: Vector[String]

  /**
    * Returns the second largest subdomain for this URL's host.
    *
    * E.g. for http://a.b.c.example.com returns a.b.c
    *
    * Note: In the event there is only one subdomain (i.e. the host is the apex domain), this method returns `None`.
    * E.g. This method will return `None` for `http://example.com`.
    *
    * @return the second largest subdomain for this URL's host
    */
  def subdomain: Option[String]

  /**
    * Returns all subdomains for this URL's host.
    * E.g. for http://a.b.c.example.com returns a, a.b, a.b.c and a.b.c.example
    * @return all subdomains for this URL's host
    */
  def subdomains: Vector[String]

  /**
    * Returns the shortest subdomain for this URL's host.
    * E.g. for http://a.b.c.example.com returns a
    * @return the shortest subdomain for this URL's host
    */
  def shortestSubdomain: Option[String]

  /**
    * Returns the longest subdomain for this URL's host.
    * E.g. for http://a.b.c.example.com returns a.b.c.example
    * @return the longest subdomain for this URL's host
    */
  def longestSubdomain: Option[String]

  /**
    * Copies this Url but with the authority set as the given value.
    *
    * @param authority the authority host to set
    * @return a new Url with the specified authority
    */
  def withAuthority(authority: Authority): SelfWithAuthority

  /**
    * Copies this Url but with the host set as the given value.
    *
    * @param host the new host to set
    * @return a new Url with the specified host
    */
  def withHost(host: Host): SelfWithAuthority

  /**
    * Copies this Url but with the host set as the given value.
    *
    * @param host the new host to set
    * @return a new Url with the specified host
    */
  def withHost(host: String): SelfWithAuthority =
    withHost(Host.parse(host))

  /**
    * Copies this Url but with the fragment set as the given value.
    *
    * @param fragment the new fragment to set
    * @return a new Url with the specified fragment
    */
  def withFragment(fragment: String): Self =
    withFragment(Some(fragment))

  /**
    * Copies this Url but with the fragment set as the given value.
    *
    * @param fragment the new fragment to set
    * @return a new Url with the specified fragment
    */
  def withFragment(fragment: Option[String]): Self

  /**
    * Copies this Url but with the path set as the given value.
    *
    * @param path the new path to set
    * @return a new Url with the specified path
    */
  def withPath(path: UrlPath): Self

  /**
    * Copies this Url but with the query set as the given value.
    *
    * @param query the new QueryString to set
    * @return a new Url with the specified query
    */
  def withQueryString(query: QueryString): Self

  /**
    * Replaces the all existing Query String parameters with a new set of query params
    *
    * Pairs with values, such as ("k", Some("v")), represent query params with values, i.e ?k=v
    * Pairs without values, such as ("k", None), represent query params without values, i.e ?k
    */
  def withQueryStringOptionValues(params: (String, Option[String])*): Self =
    withQueryString(QueryString(params.toVector))

  /**
    * Replaces the all existing Query String parameters with a new set of query params
    */
  def withQueryString(params: (String, String)*): Self =
    withQueryString(QueryString.fromTraversable(params))

  def addPathPart(part: String): Self =
    withPath(path.addPart(part))

  def addPathParts(parts: Iterable[String]): Self =
    withPath(path.addParts(parts))

  def addPathParts(parts: String*): Self =
    withPath(path.addParts(parts))

  /**
    * Adds a new Query String parameter key-value pair.
    *
    * Pairs with values, such as `Some("value")`, represent query params with values, i.e `?param=value`
    *
    * By default, pairs without values, i.e `None`, represent query params without values, i.e `?param`
    * Using a `UriConfig(renderQuery = ExcludeNones)`, will cause pairs with `None` values not to be rendered
    *
    * @param name name of the parameter
    * @param value value for the parameter
    * @return A new Url with the new Query String parameter
    */
  def addParam(name: String, value: Option[String]): Self =
    withQueryString(query.addParam(name, value))

  /**
    * Adds a new Query String parameter key-value pair.
    *
    * @param name name of the parameter
    * @param value value for the parameter
    * @return A new Url with the new Query String parameter
    */
  def addParam(name: String, value: String): Self =
    withQueryString(query.addParam(name, value))

  /**
    * Adds a new Query String parameter key-value pair.
    *
    * @param kv name-value pair for the query parameter to be added
    * @return A new Url with the new Query String parameter
    */
  def addParam(kv: (String, String)): Self =
    withQueryString(query.addParam(kv))

  /**
    * Adds a new Query String parameter key-value pair.
    *
    * Pairs with values, such as `("param", Some("value"))`, represent query params with values, i.e `?param=value`
    *
    * By default, pairs without values, such as `("param", None)`, represent query params without values, i.e `?param`
    * Using a `UriConfig(renderQuery = ExcludeNones)`, will cause pairs with `None` values not to be rendered
    *
    * @param kv name-value pair for the query parameter to be added
    * @return A new Url with the new Query String parameter
    */
  def addParamOptionValue(kv: (String, Option[String])): Self =
    withQueryString(query.addParamOptionValue(kv))

  /**
    * Adds all the specified key-value pairs as parameters to the query
    *
    * @param kvs A list of key-value pairs to add as query parameters
    * @return A new Url with the new Query String parameters
    */
  def addParams(kvs: (String, String)*): Self =
    withQueryString(query.addParams(kvs: _*))

  /**
    * Adds all the specified key-value pairs as parameters to the query
    *
    * @param kvs A list of key-value pairs to add as query parameters
    * @return A new Url with the new Query String parameters
    */
  def addParams(kvs: Iterable[(String, String)]): Self =
    withQueryString(query.addParams(kvs))

  /**
    * Adds all the specified key-value pairs as parameters to the query
    *
    * Pairs with values, such as `("param", Some("value"))`, represent query params with values, i.e `?param=value`
    *
    * By default, pairs without values, such as `("param", None)`, represent query params without values, i.e `?param`
    * Using a `UriConfig(renderQuery = ExcludeNones)`, will cause pairs with `None` values not to be rendered
    *
    * @param kvs A list of key-value pairs to add as query parameters
    * @return A new Url with the new Query String parameters
    */
  def addParamsOptionValues(kvs: Iterable[(String, Option[String])]): Self =
    withQueryString(query.addParamsOptionValues(kvs))

  /**
    * Adds all the specified key-value pairs as parameters to the query
    *
    * Pairs with values, such as `("param", Some("value"))`, represent query params with values, i.e `?param=value`
    *
    * By default, pairs without values, such as `("param", None)`, represent query params without values, i.e `?param`
    * Using a `UriConfig(renderQuery = ExcludeNones)`, will cause pairs with `None` values not to be rendered
    *
    * @param kvs A list of key-value pairs to add as query parameters
    * @return A new Url with the new Query String parameters
    */
  def addParamsOptionValues(kvs: (String, Option[String])*): Self =
    withQueryString(query.addParamsOptionValues(kvs: _*))

  /**
    * Replaces the all existing Query String parameters with the specified key with a single Query String parameter
    * with the specified value.
    *
    * Pairs with values, such as `("param", Some("value"))`, represent query params with values, i.e `?param=value`
    *
    * By default, pairs without values, such as `("param", None)`, represent query params without values, i.e `?param`
    * Using a `UriConfig(renderQuery = ExcludeNones)`, will cause pairs with `None` values not to be rendered
    *
    * @param k Key for the Query String parameter(s) to replace
    * @param v value to replace with
    * @return A new Uri with the result of the replace
    */
  def replaceParams(k: String, v: Option[String]): Self =
    withQueryString(query.replaceAll(k, v))

  /**
    * Replaces the all existing Query String parameters with the specified key with a single Query String parameter
    * with the specified value.
    *
    * @param k Key for the Query String parameter(s) to replace
    * @param v value to replace with
    * @return A new Uri with the result of the replace
    */
  def replaceParams(k: String, v: String): Self =
    withQueryString(query.replaceAll(k, v))

  /**
    * Removes all Query String parameters with the specified key
    * @param k Key for the Query String parameter(s) to remove
    * @return
    */
  def removeParams(k: String): Self =
    withQueryString(query.removeAll(k))

  /**
    * Removes all Query String parameters with a name in the specified list
    * @param k Names of Query String parameter(s) to remove
    * @return
    */
  def removeParams(k: String*): Self =
    withQueryString(query.removeAll(k))

  /**
    * Removes all Query String parameters with a name in the specified list
    * @param k Names of Query String parameter(s) to remove
    * @return
    */
  def removeParams(k: Iterable[String]): Self =
    withQueryString(query.removeAll(k))

  /**
    * Removes all Query String parameters
    * @return
    */
  def removeQueryString(): Self =
    withQueryString(QueryString.empty)

  /**
    * Transforms the Query String by applying the specified PartialFunction to each Query String Parameter
    *
    * Parameters not defined in the PartialFunction will be left as-is.
    *
    * @param f A function that returns a new Parameter when applied to each Parameter
    * @return
    */
  def mapQuery(f: PartialFunction[(String, Option[String]), (String, Option[String])]): Self =
    withQueryString(query.map(f))

  /**
    * Transforms the Query String by applying the specified PartialFunction to each Query String Parameter
    *
    * Parameters not defined in the PartialFunction will be removed.
    *
    * @param f A function that returns a new Parameter when applied to each Parameter
    * @return
    */
  def collectQuery(f: PartialFunction[(String, Option[String]), (String, Option[String])]): Self =
    withQueryString(query.collect(f))

  /**
    * Transforms the Query String by applying the specified Function to each Query String Parameter
    *
    * @param f A function that returns a collection of Parameters when applied to each parameter
    * @return
    */
  def flatMapQuery(f: ((String, Option[String])) => Iterable[(String, Option[String])]): Self =
    withQueryString(query.flatMap(f))

  /**
    * Transforms the Query String by applying the specified Function to each Query String Parameter name
    *
    * @param f A function that returns a new Parameter name when applied to each Parameter name
    * @return
    */
  def mapQueryNames(f: String => String): Self =
    withQueryString(query.mapNames(f))

  /**
    * Transforms the Query String by applying the specified Function to each Query String Parameter value
    *
    * @param f A function that returns a new Parameter value when applied to each Parameter value
    * @return
    */
  def mapQueryValues(f: String => String): Self =
    withQueryString(query.mapValues(f))

  /**
    * Removes any Query String Parameters that return false when applied to the given Function
    *
    * @param f
    * @return
    */
  def filterQuery(f: ((String, Option[String])) => Boolean): Self =
    withQueryString(query.filter(f))

  /**
    * Removes any Query String Parameters that return false when their name is applied to the given Function
    *
    * @param f
    * @return
    */
  def filterQueryNames(f: String => Boolean): Self =
    withQueryString(query.filterNames(f))

  /**
    * Returns the apex domain for this URL.
    *
    * The apex domain is constructed from the public suffix for this URL's host prepended with the
    * immediately preceding dot segment.
    *
    * Examples include:
    *  `example.com`   for `www.example.com`
    *  `example.co.uk` for `www.example.co.uk`
    *
    * @return the apex domain for this URL
    */
  def apexDomain: Option[String] =
    hostOption.flatMap(_.apexDomain)

  /**
    * Removes any Query String Parameters that return false when their value is applied to the given Function
    *
    * @param f
    * @return
    */
  def filterQueryValues(f: String => Boolean): Self =
    withQueryString(query.filterValues(f))

  private[uri] def fragmentToString(c: UriConfig): String =
    fragment.map(f => "#" + c.fragmentEncoder.encode(f, c.charset)).getOrElse("")

  def toAbsoluteUrl: AbsoluteUrl = this match {
    case a: AbsoluteUrl => a
    case _              => throw new UriConversionException(getClass.getSimpleName + " cannot be converted to AbsoluteUrl")
  }

  def toRelativeUrl: RelativeUrl = this match {
    case r: RelativeUrl => r
    case _              => RelativeUrl(path, query, fragment)
  }

  def toProtocolRelativeUrl: ProtocolRelativeUrl = this match {
    case p: ProtocolRelativeUrl => p
    case a: AbsoluteUrl         => ProtocolRelativeUrl(a.authority, a.path, a.query, a.fragment)
    case _                      => throw new UriConversionException(getClass.getSimpleName + " cannot be converted to ProtocolRelativeUrl")
  }

  def toUrl: Url = this
  def toUrn: Urn = throw new UriConversionException(getClass.getSimpleName + " cannot be converted to Urn")

  /**
    * @return the URL as a String. If the URI has a domain name for a host, any unicode characters will be
    *         returned in ASCII Compatible Encoding (ACE), as defined by the ToASCII operation of
    *         <a href="http://www.ietf.org/rfc/rfc3490.txt">RFC 3490</a>.
    */
  def toStringPunycode: String =
    toString(config)

  protected def queryToString(config: UriConfig): String =
    query.toString(config) match {
      case "" => ""
      case s  => "?" + s
    }
}

object Url {

  def apply(scheme: String = null,
            user: String = null,
            password: String = null,
            host: String = null,
            port: Int = -1,
            path: String = "",
            query: QueryString = QueryString.empty,
            fragment: String = null)(implicit config: UriConfig = UriConfig.default): Url = {

    val urlPath = UrlPath.parse(path)
    val frag = Option(fragment)
    def authority = {
      val portOpt = if (port > 0) Some(port) else None
      val userInfo = Option(user).map(u => UserInfo(u, Option(password)))
      Authority(userInfo, Host.parse(host), portOpt)
    }

    (scheme, host) match {
      case (null, null) => RelativeUrl(urlPath, query, frag)
      case (_, null)    => UrlWithoutAuthority(scheme, urlPath, query, frag)
      case (null, _)    => ProtocolRelativeUrl(authority, urlPath.toAbsoluteOrEmpty, query, frag)
      case (_, _)       => AbsoluteUrl(scheme, authority, urlPath.toAbsoluteOrEmpty, query, frag)
    }
  }

  def unapply(url: Url): Option[(UrlPath, QueryString, Option[String])] =
    Some((url.path, url.query, url.fragment))

  def parse(s: CharSequence)(implicit config: UriConfig = UriConfig.default): Url =
    parseTry(s).get

  def parseOption(s: CharSequence)(implicit config: UriConfig = UriConfig.default): Option[Url] =
    parseTry(s).toOption

  def parseTry(s: CharSequence)(implicit config: UriConfig = UriConfig.default): Try[Url] =
    UrlParser.parseUrl(s.toString)

  implicit val eqUrl: Eq[Url] = Eq.fromUniversalEquals
  implicit val showUrl: Show[Url] = Show.fromToString
  implicit val orderUrl: Order[Url] = Order.by(_.toString())
}

/**
  * Represents Relative URLs which do not contain an authority. Examples include:
  *
  *  -      Root Relative: `/index.html?a=b`
  *  -  Rootless Relative: `index.html?a=b`
  *  -  Rootless Relative
  *    (with dot segment): `../index.html?a=b`
  */
final case class RelativeUrl(path: UrlPath, query: QueryString, fragment: Option[String])(
    implicit val config: UriConfig = UriConfig.default
) extends Url {

  type Self = RelativeUrl
  type SelfWithAuthority = ProtocolRelativeUrl
  type SelfWithScheme = UrlWithoutAuthority

  def self: RelativeUrl = this

  def schemeOption: Option[String] = None

  def hostOption: Option[Host] = None
  def port: Option[Int] = None

  def user: Option[String] = None
  def password: Option[String] = None

  def publicSuffix: Option[String] = None
  def publicSuffixes: Vector[String] = Vector.empty
  def subdomain: Option[String] = None
  def subdomains: Vector[String] = Vector.empty
  def shortestSubdomain: Option[String] = None
  def longestSubdomain: Option[String] = None

  def withScheme(scheme: String): UrlWithoutAuthority =
    UrlWithoutAuthority(scheme, path, query, fragment)

  def withAuthority(authority: Authority): ProtocolRelativeUrl =
    ProtocolRelativeUrl(authority, path.toAbsoluteOrEmpty, query, fragment)

  def withHost(host: Host): ProtocolRelativeUrl =
    withAuthority(Authority(host))

  /**
    * Copies this Url but with the path set as the given value.
    *
    * @param path the new path to set
    * @return a new Url with the specified path
    */
  def withPath(path: UrlPath): RelativeUrl =
    copy(path = path)

  def withFragment(fragment: Option[String]): RelativeUrl =
    copy(fragment = fragment)

  def withQueryString(query: QueryString): RelativeUrl =
    copy(query = query)

  private[uri] def toString(c: UriConfig): String =
    path.toString(c) + queryToString(c) + fragmentToString(c)
}

object RelativeUrl {
  def empty: RelativeUrl =
    RelativeUrl(UrlPath.empty, QueryString.empty, None)

  def parse(s: CharSequence)(implicit config: UriConfig = UriConfig.default): RelativeUrl =
    parseTry(s).get

  def parseOption(s: CharSequence)(implicit config: UriConfig = UriConfig.default): Option[RelativeUrl] =
    parseTry(s).toOption

  def parseTry(s: CharSequence)(implicit config: UriConfig = UriConfig.default): Try[RelativeUrl] =
    UrlParser.parseRelativeUrl(s.toString)

  implicit val eqRelUrl: Eq[RelativeUrl] = Eq.fromUniversalEquals
  implicit val showRelUrl: Show[RelativeUrl] = Show.fromToString
  implicit val orderRelUrl: Order[RelativeUrl] = Order.by { url =>
    (url.path, url.query, url.fragment)
  }
}

/**
  * Represents absolute URLs with an authority (i.e. URLs with a host), examples include:
  *
  *  -          Absolute URL: `http://example.com`
  *  - Protocol Relative URL: `//example.com`
  */
sealed trait UrlWithAuthority extends Url {

  type Self <: UrlWithAuthority
  type SelfWithScheme <: UrlWithAuthority
  type SelfWithAuthority = Self

  def authority: Authority

  def host: Host = authority.host
  def hostOption: Option[Host] = Some(host)

  def port: Option[Int] = authority.port
  def userInfo: Option[UserInfo] = authority.userInfo
  def user: Option[String] = authority.user
  def password: Option[String] = authority.password

  def withHost(host: Host): Self =
    withAuthority(authority.copy(host = host))

  /**
    * Copies this Url but with the user set as the given value.
    *
    * @param user the new user to set
    * @return a new Url with the specified user
    */
  def withUser(user: String): Self = {
    withAuthority(authority.copy(userInfo = Some(UserInfo(user, password))))
  }

  /**
    * Copies this Url but with the password set as the given value.
    *
    * @param password the new password to set
    * @return a new Url with the specified password
    */
  def withPassword(password: String): Self = {
    withAuthority(authority.copy(userInfo = Some(UserInfo(user.getOrElse(""), password))))
  }

  /**
    * Copies this Url but with the port set as the given value.
    *
    * @param port the new port to set
    * @return a new Url with the specified port
    */
  def withPort(port: Int): Self =
    withAuthority(authority.copy(port = Some(port)))

  /**
    * Returns the longest public suffix for the host in this URI. Examples include:
    *  `com`   for `www.example.com`
    *  `co.uk` for `www.example.co.uk`
    *
    * @return the longest public suffix for the host in this URI
    */
  def publicSuffix: Option[String] =
    authority.publicSuffix

  /**
    * Returns all longest public suffixes for the host in this URI. Examples include:
    *  `com` for `www.example.com`
    *  `co.uk` and `uk` for `www.example.co.uk`
    *
    * @return all public suffixes for the host in this URI
    */
  def publicSuffixes: Vector[String] =
    authority.publicSuffixes

  /**
    * Returns the second largest subdomain for this URL's host.
    *
    * E.g. for http://a.b.c.example.com returns a.b.c
    *
    * Note: In the event there is only one subdomain (i.e. the host is the apex domain), this method returns `None`.
    * E.g. This method will return `None` for `http://example.com`.
    *
    * @return the second largest subdomain for this URL's host
    */
  def subdomain: Option[String] =
    authority.subdomain

  /**
    * Returns all subdomains for this URL's host.
    * E.g. for http://a.b.c.example.com returns a, a.b, a.b.c and a.b.c.example
    * @return all subdomains for this URL's host
    */
  def subdomains: Vector[String] =
    authority.subdomains

  /**
    * Returns the shortest subdomain for this URL's host.
    * E.g. for http://a.b.c.example.com returns a
    * @return the shortest subdomain for this URL's host
    */
  def shortestSubdomain: Option[String] =
    authority.shortestSubdomain

  /**
    * Returns the longest subdomain for this URL's host.
    * E.g. for http://a.b.c.example.com returns a.b.c.example
    * @return the longest subdomain for this URL's host
    */
  def longestSubdomain: Option[String] =
    authority.longestSubdomain

  /**
    * @return the URL as a String. If the URI has a domain name for a host, any unicode characters will be
    *         returned in ASCII Compatible Encoding (ACE), as defined by the ToASCII operation of
    *         <a href="http://www.ietf.org/rfc/rfc3490.txt">RFC 3490</a>.
    */
  override def toStringPunycode: String =
    toString(config, _.toStringPunycode)

  private[uri] def toString(c: UriConfig): String =
    toString(c, _.toString)

  private[uri] def toString(c: UriConfig, hostToString: Host => String): String
}

object UrlWithAuthority {

  def unapply(url: UrlWithAuthority): Option[(Authority, UrlPath, QueryString, Option[String])] =
    Some((url.authority, url.path, url.query, url.fragment))

  def parse(s: CharSequence)(implicit config: UriConfig = UriConfig.default): UrlWithAuthority =
    parseTry(s).get

  def parseOption(s: CharSequence)(implicit config: UriConfig = UriConfig.default): Option[UrlWithAuthority] =
    parseTry(s).toOption

  def parseTry(s: CharSequence)(implicit config: UriConfig = UriConfig.default): Try[UrlWithAuthority] =
    UrlParser.parseUrlWithAuthority(s.toString)

  implicit val eqUrlWithAuthority: Eq[UrlWithAuthority] = Eq.fromUniversalEquals
  implicit val showUrlWithAuthority: Show[UrlWithAuthority] = Show.fromToString
  implicit val orderUrlWithAuthority: Order[UrlWithAuthority] = Order.by(_.toString())
}

/**
  * Represents absolute URLs, for example: `//example.com`
  */
final case class ProtocolRelativeUrl(authority: Authority,
                                     path: AbsoluteOrEmptyPath,
                                     query: QueryString,
                                     fragment: Option[String])(implicit val config: UriConfig = UriConfig.default)
    extends UrlWithAuthority {

  type Self = ProtocolRelativeUrl
  type SelfWithScheme = AbsoluteUrl

  def self: ProtocolRelativeUrl = this

  def schemeOption: Option[String] = None

  def withScheme(scheme: String): AbsoluteUrl =
    AbsoluteUrl(scheme, authority, path, query, fragment)

  def withAuthority(authority: Authority): ProtocolRelativeUrl =
    copy(authority = authority)

  def withFragment(fragment: Option[String]): ProtocolRelativeUrl =
    copy(fragment = fragment)

  /**
    * Copies this Url but with the path set as the given value.
    *
    * If the specified path is non empty *and* doesn't have a leading slash, one will be added, as per RFC 3986:
    * When authority is present, the path must either be empty or begin with a slash ("/") character.
    *
    * @param path the new path to set
    * @return a new Url with the specified path
    */
  def withPath(path: UrlPath): ProtocolRelativeUrl =
    copy(path = path.toAbsoluteOrEmpty)

  def withQueryString(query: QueryString): ProtocolRelativeUrl =
    copy(query = query)

  private[uri] def toString(c: UriConfig, hostToString: Host => String): String =
    "//" + authority.toString(c, hostToString) + path.toString(c) + queryToString(c) + fragmentToString(c)
}

object ProtocolRelativeUrl {
  def parse(s: CharSequence)(implicit config: UriConfig = UriConfig.default): ProtocolRelativeUrl =
    parseTry(s).get

  def parseOption(s: CharSequence)(implicit config: UriConfig = UriConfig.default): Option[ProtocolRelativeUrl] =
    parseTry(s).toOption

  def parseTry(s: CharSequence)(implicit config: UriConfig = UriConfig.default): Try[ProtocolRelativeUrl] =
    UrlParser.parseProtocolRelativeUrl(s.toString)

  implicit val eqProtocolRelUrl: Eq[ProtocolRelativeUrl] = Eq.fromUniversalEquals
  implicit val showProtocolRelUrl: Show[ProtocolRelativeUrl] = Show.fromToString
  implicit val orderProtocolRelUrl: Order[ProtocolRelativeUrl] = Order.by { url =>
    (url.authority, url.path, url.query, url.fragment)
  }
}

/**
  * Represents absolute URLs, for example: `http://example.com`
  */
final case class AbsoluteUrl(scheme: String,
                             authority: Authority,
                             path: AbsoluteOrEmptyPath,
                             query: QueryString,
                             fragment: Option[String])(implicit val config: UriConfig = UriConfig.default)
    extends UrlWithAuthority {

  type Self = AbsoluteUrl
  type SelfWithScheme = AbsoluteUrl

  def self: AbsoluteUrl = this

  def schemeOption: Option[String] = Some(scheme)

  def withScheme(scheme: String): AbsoluteUrl =
    AbsoluteUrl(scheme, authority, path, query, fragment)

  def withAuthority(authority: Authority): AbsoluteUrl =
    copy(authority = authority)

  def withFragment(fragment: Option[String]): AbsoluteUrl =
    copy(fragment = fragment)

  /**
    * Copies this Url but with the path set as the given value.
    *
    * If the specified path is non empty *and* doesn't have a leading slash, one will be added, as per RFC 3986:
    * When authority is present, the path must either be empty or begin with a slash ("/") character.
    *
    * @param path the new path to set
    * @return a new Url with the specified path
    */
  def withPath(path: UrlPath): AbsoluteUrl =
    copy(path = path.toAbsoluteOrEmpty)

  def withQueryString(query: QueryString): AbsoluteUrl =
    copy(query = query)

  private[uri] def toString(c: UriConfig, hostToString: Host => String): String =
    scheme + "://" + authority.toString(c, hostToString) + path.toString(c) + queryToString(c) + fragmentToString(c)
}

object AbsoluteUrl {
  def parse(s: CharSequence)(implicit config: UriConfig = UriConfig.default): AbsoluteUrl =
    parseTry(s).get

  def parseOption(s: CharSequence)(implicit config: UriConfig = UriConfig.default): Option[AbsoluteUrl] =
    parseTry(s).toOption

  def parseTry(s: CharSequence)(implicit config: UriConfig = UriConfig.default): Try[AbsoluteUrl] =
    UrlParser.parseAbsoluteUrl(s.toString)

  implicit val eqAbsUrl: Eq[AbsoluteUrl] = Eq.fromUniversalEquals
  implicit val showAbsUrl: Show[AbsoluteUrl] = Show.fromToString
  implicit val orderAbsUrl: Order[AbsoluteUrl] = Order.by { url =>
    (url.scheme, url.authority, url.path, url.query, url.fragment)
  }
}

/**
  * Represents URLs that do not have an authority, for example: `mailto:example@example.com`
  */
sealed trait UrlWithoutAuthority extends Url {

  type Self <: UrlWithoutAuthority
  type SelfWithScheme <: UrlWithoutAuthority
  type SelfWithAuthority = AbsoluteUrl

  def scheme: String

  def hostOption: Option[Host] = None
  def port: Option[Int] = None
  def user: Option[String] = None
  def password: Option[String] = None

  def publicSuffix: Option[String] = None
  def publicSuffixes: Vector[String] = Vector.empty
  def subdomain: Option[String] = None
  def subdomains: Vector[String] = Vector.empty
  def shortestSubdomain: Option[String] = None
  def longestSubdomain: Option[String] = None

}

object UrlWithoutAuthority {

  def apply(scheme: String, path: UrlPath, query: QueryString, fragment: Option[String])(
      implicit config: UriConfig = UriConfig.default
  ): UrlWithoutAuthority =
    SimpleUrlWithoutAuthority(scheme, path, query, fragment)(config)

  def unapply(url: UrlWithoutAuthority): Option[(String, UrlPath, QueryString, Option[String])] =
    Some((url.scheme, url.path, url.query, url.fragment))

  def parse(s: CharSequence)(implicit config: UriConfig = UriConfig.default): UrlWithoutAuthority =
    parseTry(s).get

  def parseOption(s: CharSequence)(implicit config: UriConfig = UriConfig.default): Option[UrlWithoutAuthority] =
    parseTry(s).toOption

  def parseTry(s: CharSequence)(implicit config: UriConfig = UriConfig.default): Try[UrlWithoutAuthority] =
    UrlParser.parseUrlWithoutAuthority(s.toString)

  implicit val eqUrlWithoutAuthority: Eq[UrlWithoutAuthority] = Eq.fromUniversalEquals
  implicit val showUrlWithoutAuthority: Show[UrlWithoutAuthority] = Show.fromToString
  implicit val orderUrlWithoutAuthority: Order[UrlWithoutAuthority] = Order.by { url =>
    (url.scheme, url.path, url.query, url.fragment)
  }
}

final case class SimpleUrlWithoutAuthority(scheme: String, path: UrlPath, query: QueryString, fragment: Option[String])(
    implicit val config: UriConfig = UriConfig.default
) extends UrlWithoutAuthority {

  type Self = SimpleUrlWithoutAuthority
  type SelfWithScheme = SimpleUrlWithoutAuthority

  def self: SimpleUrlWithoutAuthority = this

  def schemeOption: Option[String] = Some(scheme)

  def withScheme(scheme: String): SimpleUrlWithoutAuthority =
    copy(scheme = scheme)

  /**
    * Copies this Url but with the host set as the given value.
    *
    * @param host the new host to set
    * @return a new Url with the specified host
    */
  def withHost(host: Host): AbsoluteUrl =
    AbsoluteUrl(scheme, Authority(host), path.toAbsoluteOrEmpty, query, fragment)

  /**
    * Copies this Url but with the path set as the given value.
    *
    * @param path the new path to set
    * @return a new Url with the specified path
    */
  def withPath(path: UrlPath): SimpleUrlWithoutAuthority =
    copy(path = path)

  /**
    * Copies this Url but with the port set as the given value.
    *
    * @param port the new port to set
    * @return a new Url with the specified port
    */
  def withPort(port: Int): AbsoluteUrl =
    AbsoluteUrl(scheme, Authority(host = "", port), path.toAbsoluteOrEmpty, query, fragment)

  def withAuthority(authority: Authority): AbsoluteUrl =
    AbsoluteUrl(scheme, authority, path.toAbsoluteOrEmpty, query, fragment)

  def withFragment(fragment: Option[String]): SimpleUrlWithoutAuthority =
    copy(fragment = fragment)

  def withQueryString(query: QueryString): SimpleUrlWithoutAuthority =
    copy(query = query)

  private[uri] def toString(c: UriConfig): String =
    scheme + ":" + path.toString(c) + queryToString(c) + fragmentToString(c)
}

object SimpleUrlWithoutAuthority {
  def parse(s: CharSequence)(implicit config: UriConfig = UriConfig.default): SimpleUrlWithoutAuthority =
    parseTry(s).get

  def parseOption(s: CharSequence)(implicit config: UriConfig = UriConfig.default): Option[SimpleUrlWithoutAuthority] =
    parseTry(s).toOption

  def parseTry(s: CharSequence)(implicit config: UriConfig = UriConfig.default): Try[SimpleUrlWithoutAuthority] =
    UrlParser.parseSimpleUrlWithoutAuthority(s.toString)

  implicit val eqSimpleUrlWithoutAuthority: Eq[SimpleUrlWithoutAuthority] = Eq.fromUniversalEquals
  implicit val showSimpleUrlWithoutAuthority: Show[SimpleUrlWithoutAuthority] = Show.fromToString
  implicit val orderSimpleUrlWithoutAuthority: Order[SimpleUrlWithoutAuthority] = Order.by { url =>
    (url.scheme, url.path, url.query, url.fragment)
  }
}

/**
  * Represents URLs with the data scheme, for example: `data:text/plain;charset=UTF-8;page=21,the%20data:1234,5678`
  */
final case class DataUrl(mediaType: MediaType, base64: Boolean, data: Array[Byte])(implicit val config: UriConfig =
                                                                                     UriConfig.default)
    extends UrlWithoutAuthority {

  type Self = DataUrl
  type SelfWithScheme = UrlWithoutAuthority

  def self: DataUrl = this

  val scheme = "data"
  def schemeOption: Option[String] = Some(scheme)

  def query: QueryString = QueryString.empty
  def fragment: Option[String] = None
  def path: UrlPath = RootlessPath.fromParts(pathString(config.withNoEncoding))

  /**
    * @return The data from this data URL using the charset provided by the URL's mediatype
    */
  def dataAsString: String =
    new String(data, mediaType.charset)

  protected def pathString(c: UriConfig): String = {
    val base64Str = if (base64) ";base64" else ""
    val dataStr =
      if (base64) {
        val b64Encoded = Base64.getEncoder.encodeToString(data)
        c.pathEncoder.encode(b64Encoded, mediaType.charset)
      } else {
        c.pathEncoder.encode(data, mediaType.charset)
      }
    mediaType.toString + base64Str + "," + dataStr
  }

  def withScheme(scheme: String): UrlWithoutAuthority =
    if (scheme.equalsIgnoreCase("data"))
      this
    else
      SimpleUrlWithoutAuthority(scheme, path, query, fragment)

  /**
    * Copies this Url but with the host set as the given value.
    *
    * @param host the new host to set
    * @return a new Url with the specified host
    */
  def withHost(host: Host): AbsoluteUrl =
    AbsoluteUrl(scheme, Authority(host), path.toAbsoluteOrEmpty, query, fragment)

  /**
    * Copies this Url but with the path set as the given value.
    *
    * @param path the new path to set
    * @return a new Url with the specified path
    */
  def withPath(path: UrlPath): DataUrl =
    DataUrl.parse(scheme + ":" + path.toString())

  /**
    * Copies this Url but with the port set as the given value.
    *
    * @param port the new port to set
    * @return a new Url with the specified port
    */
  def withPort(port: Int): AbsoluteUrl =
    AbsoluteUrl(scheme, Authority(host = "", port), path.toAbsoluteOrEmpty, query, fragment)

  def withAuthority(authority: Authority): AbsoluteUrl =
    AbsoluteUrl(scheme, authority, path.toAbsoluteOrEmpty, query, fragment)

  def withFragment(fragment: Option[String]): DataUrl =
    this

  def withQueryString(query: QueryString): DataUrl =
    this

  private[uri] def toString(c: UriConfig): String =
    scheme + ":" + pathString(c)
}

object DataUrl {
  def fromBase64(mediaType: MediaType, data: String)(implicit config: UriConfig = UriConfig.default): DataUrl = {
    val base64Data = config.pathDecoder.decodeBytes(data, mediaType.charset)
    DataUrl(mediaType, base64 = true, Base64.getDecoder.decode(base64Data))
  }

  def fromPercentEncoded(mediaType: MediaType, data: String)(implicit config: UriConfig = UriConfig.default): DataUrl =
    DataUrl(mediaType, base64 = false, config.pathDecoder.decodeBytes(data, mediaType.charset))

  def parse(s: CharSequence)(implicit config: UriConfig = UriConfig.default): DataUrl =
    parseTry(s).get

  def parseOption(s: CharSequence)(implicit config: UriConfig = UriConfig.default): Option[DataUrl] =
    parseTry(s).toOption

  def parseTry(s: CharSequence)(implicit config: UriConfig = UriConfig.default): Try[DataUrl] =
    UrlParser.parseDataUrl(s.toString)

  implicit val eqDataUrl: Eq[DataUrl] = Eq.fromUniversalEquals
  implicit val showDataUrl: Show[DataUrl] = Show.fromToString
  implicit val orderDataUrl: Order[DataUrl] = Order.by(_.toString())
}

/**
  * Represents a URN. See [[https://www.ietf.org/rfc/rfc2141 RFC 2141]]
  * and [[https://tools.ietf.org/html/rfc8141 RFC 8141]]
  *
  * URNs will be in the form `urn:nid:nss`
  */
final case class Urn(path: UrnPath)(implicit val config: UriConfig = UriConfig.default) extends Uri {

  type Self = Urn
  type SelfWithScheme = UrlWithoutAuthority

  def self: Self = this

  def scheme = "urn"
  def schemeOption = Some(scheme)

  def nss: String = path.nss
  def nid: String = path.nid

  /**
    * Converts this URN into a URL with the given scheme.
    * The NID and NSS will be made path segments of the URL.
    *
    * @param scheme the new scheme to set
    * @return a new Uri with the specified scheme
    */
  def withScheme(scheme: String): UrlWithoutAuthority =
    UrlWithoutAuthority(scheme, path.toUrlPath, QueryString.empty, fragment = None)

  def toUrl: Url = throw new UriConversionException("Urn cannot be converted to Url")
  def toUrn: Urn = this

  private[uri] def toString(c: UriConfig): String =
    scheme + ":" + path.toString(c)
}

object Urn {
  def apply(nid: String, nss: String): Urn =
    new Urn(UrnPath(nid, nss))

  def parse(s: CharSequence)(implicit config: UriConfig = UriConfig.default): Urn =
    parseTry(s).get

  def parseOption(s: CharSequence)(implicit config: UriConfig = UriConfig.default): Option[Urn] =
    parseTry(s).toOption

  def parseTry(s: CharSequence)(implicit config: UriConfig = UriConfig.default): Try[Urn] =
    UrnParser.parseUrn(s.toString)

  implicit val eqUrn: Eq[Urn] = Eq.fromUniversalEquals
  implicit val showUrn: Show[Urn] = Show.fromToString
  implicit val orderUrn: Order[Urn] = Order.by(_.path)
}
