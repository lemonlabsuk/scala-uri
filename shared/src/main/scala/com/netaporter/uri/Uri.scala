package com.netaporter.uri

import com.netaporter.uri.Parameters.Param
import com.netaporter.uri.config.UriConfig
import com.netaporter.uri.inet.PublicSuffixes
import com.netaporter.uri.parsing.UriParser

import scala.collection.{GenTraversableOnce, Seq}

/**
 * http://tools.ietf.org/html/rfc3986
 */
case class Uri (
  scheme: Option[String],
  user: Option[String],
  password: Option[String],
  host: Option[String],
  port: Option[Int],
  pathParts: Seq[PathPart],
  query: QueryString,
  fragment: Option[String],
  pathStartsWithSlash: Boolean
) extends SubdomainSupport {

  lazy val hostParts: Seq[String] =
    host.map(h => h.split('.').toVector).getOrElse(Vector.empty)

  def pathPartOption(name: String) =
    pathParts.find(_.part == name)

  def pathPart(name: String) =
    pathPartOption(name).head

  /**
   * Adds a new Query String parameter key-value pair. If the value for the Query String parmeter is None, then this
   * Query String parameter will not be rendered in calls to toString or toStringRaw
   * @param name name of the parameter
   * @param value value for the parameter
   * @return A new Uri with the new Query String parameter
   */
  def addParam(name: String, value: Any) = (name, value) match {
    case (_, None) => this
    case (n, Some(v)) => copy(query = query.addParam(n, Some(v.toString)))
    case (n, v) => copy(query = query.addParam(n, Some(v.toString)))
  }

  def addParams(kvs: Seq[(String, Any)]): Uri = {
    val cleanKvs = kvs.filterNot(_._2 == None).map {
      case (k, Some(v)) => (k, Some(v.toString))
      case (k, v) => (k, Some(v.toString))
    }
    copy(query = query.addParams(cleanKvs))
  }

  def addParams(kvs: Map[String, Any]): Uri =
    addParams(kvs.toSeq)

  def addParam(kv: Param) = copy(query = query.addParam(kv))

  def protocol = scheme

  /**
   * Copies this Uri but with the scheme set as the given value.
   *
   * @param scheme the new scheme to set
   * @return a new Uri with the specified scheme
   */
  def withScheme(scheme: String): Uri = copy(scheme = Option(scheme))

  /**
   * Copies this Uri but with the host set as the given value.
   *
   * @param host the new host to set
   * @return a new Uri with the specified host
   */
  def withHost(host: String): Uri = copy(host = Option(host))

  /**
   * Copies this Uri but with the user set as the given value.
   *
   * @param user the new user to set
   * @return a new Uri with the specified user
   */
  def withUser(user: String): Uri = copy(user = Option(user))

  /**
   * Copies this Uri but with the password set as the given value.
   *
   * @param password the new password to set
   * @return a new Uri with the specified password
   */
  def withPassword(password: String): Uri = copy(password = Option(password))

  /**
   * Copies this Uri but with the port set as the given value.
   *
   * @param port the new port to set
   * @return a new Uri with the specified port
   */
  def withPort(port: Int): Uri = copy(port = Option(port))

  /**
   * Copies this Uri but with the fragment set as the given value.
   *
   * @param fragment the new fragment to set
   * @return a new Uri with the specified fragment
   */
  def withFragment(fragment: String): Uri = copy(fragment = Option(fragment))

  /**
   * Returns the path with no encoders taking place (e.g. non ASCII characters will not be percent encoded)
   * @return String containing the raw path for this Uri
   */
  def pathRaw(implicit c: UriConfig = UriConfig.default) =
    path(c.withNoEncoding)

  /**
   * Returns the encoded path. By default non ASCII characters in the path are percent encoded.
   * @return String containing the path for this Uri
   */
  def path(implicit c: UriConfig = UriConfig.default) =
    if(pathParts.isEmpty) ""
    else (if(pathStartsWithSlash) "/" else "") + pathParts.map(_.partToString(c)).mkString("/")

  def queryStringRaw(implicit c: UriConfig = UriConfig.default) =
    queryString(c.withNoEncoding)

  def queryString(implicit c: UriConfig = UriConfig.default) =
    query.queryToString(c)

  /**
   * Replaces the all existing Query String parameters with the specified key with a single Query String parameter
   * with the specified value. If the value passed in is None, then all Query String parameters with the specified key
   * are removed
   *
   * @param k Key for the Query String parameter(s) to replace
   * @param v value to replace with
   * @return A new Uri with the result of the replace
   */
  def replaceParams(k: String, v: Any) = {
    v match {
      case valueOpt: Option[_] =>
        copy(query = query.replaceAll(k, valueOpt))
      case _ =>
        copy(query = query.replaceAll(k, Some(v)))
    }
  }

  /**
   * Replaces the all existing Query String parameters with a new set of query params
   */
  def replaceAllParams(params: Param*) =
    copy(query = query.withParams(params))

  /**
   * Transforms the Query String by applying the specified Function to each Query String Parameter
   *
   * @param f A function that returns a new Parameter when applied to each Parameter
   * @return
   */
  def mapQuery(f: Param=>Param) =
    copy(query = query.mapParams(f))

  /**
   * Transforms the Query String by applying the specified Function to each Query String Parameter
   *
   * @param f A function that returns a collection of Parameters when applied to each parameter
   * @return
   */
  def flatMapQuery(f: Param=>GenTraversableOnce[Param]) =
    copy(query = query.flatMapParams(f))

  /**
   * Transforms the Query String by applying the specified Function to each Query String Parameter name
   *
   * @param f A function that returns a new Parameter name when applied to each Parameter name
   * @return
   */
  def mapQueryNames(f: String=>String) =
    copy(query = query.mapParamNames(f))

  /**
   * Transforms the Query String by applying the specified Function to each Query String Parameter value
   *
   * @param f A function that returns a new Parameter value when applied to each Parameter value
   * @return
   */
  def mapQueryValues(f: String=>String) =
    copy(query = query.mapParamValues(f))

  /**
   * Removes any Query String Parameters that return false when applied to the given Function
   *
   * @param f
   * @return
   */
  def filterQuery(f: Param=>Boolean) =
    copy(query = query.filterParams(f))

  /**
   * Removes any Query String Parameters that return false when their name is applied to the given Function
   *
   * @param f
   * @return
   */
  def filterQueryNames(f: String=>Boolean) =
    copy(query = query.filterParamsNames(f))

  /**
   * Removes any Query String Parameters that return false when their value is applied to the given Function
   *
   * @param f
   * @return
   */
  def filterQueryValues(f: String=>Boolean) =
    copy(query = query.filterParamsValues(f))

  /**
   * Removes all Query String parameters with the specified key
   * @param k Key for the Query String parameter(s) to remove
   * @return
   */
  def removeParams(k: String) = {
    copy(query = query.removeAll(k))
  }

  /**
   * Removes all Query String parameters with the specified key contained in the a (Array)
   * @param a an Array of Keys for the Query String parameter(s) to remove
   * @return
   */
  def removeParams(a: Seq[String]) = {
    copy(query = query.removeAll(a))
  }

  /**
   * Removes all Query String parameters
   * @return
   */
  def removeAllParams() = {
    copy(query = query.removeAll())
  }

  /**
    * Returns the longest public suffix for the host in this URI. Examples include:
    *  `com`   for `www.example.com`
    *  `co.uk` for `www.example.co.uk`
    *
    * @return the longest public suffix for the host in this URI
    */
  def publicSuffix: Option[String] = {
    for {
      h <- host
      longestMatch <- PublicSuffixes.trie.longestMatch(h.reverse)
    } yield longestMatch.reverse
  }

  /**
    * Returns all longest public suffixes for the host in this URI. Examples include:
    *  `com` for `www.example.com`
    *  `co.uk` and `uk` for `www.example.co.uk`
    *
    * @return all public suffixes for the host in this URI
    */
  def publicSuffixes: Seq[String] = {
    for {
      h <- host.toSeq
      m <- PublicSuffixes.trie.matches(h.reverse)
    } yield m.reverse
  }

  override def toString = toString(UriConfig.default)

  def toString(implicit c: UriConfig = UriConfig.default): String = {
    //If there is no scheme, we use scheme relative
    def userInfo = for {
      userStr <- user
      userStrEncoded = c.userInfoEncoder.encode(userStr, c.charset)
      passwordStrEncoded = password.map(p => ":" + c.userInfoEncoder.encode(p, c.charset)).getOrElse("")
    } yield userStrEncoded + passwordStrEncoded + "@"

    val hasAuthority = user.nonEmpty || password.nonEmpty || host.nonEmpty || port.nonEmpty
    val isAbsoluteWithAuthority = scheme.nonEmpty && hasAuthority
    val isProtocolRelative = scheme.isEmpty && host.nonEmpty
    val schemeStr = scheme.map(_ + ":")

    val hostStr = for {
      hostStr <- host
      userInfoStr = userInfo.getOrElse("")
    } yield userInfoStr + hostStr

    schemeStr.getOrElse("") +
      (if(isAbsoluteWithAuthority || isProtocolRelative) "//" else "") +
      hostStr.getOrElse("") +
      port.map(":" + _).getOrElse("") +
      path(c) +
      queryString(c) +
      fragment.map(f => "#" + c.fragmentEncoder.encode(f, c.charset)).getOrElse("")
  }

  /**
   * Returns the string representation of this Uri with no encoders taking place
   * (e.g. non ASCII characters will not be percent encoded)
   * @return String containing this Uri in it's raw form
   */
  def toStringRaw(implicit config: UriConfig = UriConfig.default): String =
    toString(config.withNoEncoding)

  /**
   * Converts to a Java URI.
   * This involves a toString + URI.parse because the specific URI constructors do not deal properly with encoded elements
   * @return a URI matching this Uri
   */
  def toURI(implicit c: UriConfig = UriConfig.conservative) = new java.net.URI(toString())
}

object Uri {

  def parse(s: CharSequence)(implicit config: UriConfig = UriConfig.default): Uri =
    UriParser.parse(s.toString, config)


  def parseQuery(s: CharSequence)(implicit config: UriConfig = UriConfig.default): QueryString =
    UriParser.parseQuery(s.toString, config)

  def apply(scheme: String = null,
            user: String = null,
            password: String = null,
            host: String = null,
            port: Int = 0,
            pathParts: Seq[PathPart] = Seq.empty,
            query: QueryString = EmptyQueryString,
            fragment: String = null,
            pathStartsWithSlash: Boolean = true) = {
    new Uri(Option(scheme),
      Option(user),
      Option(password),
      Option(host),
      if(port > 0) Some(port) else None,
      pathParts,
      query,
      Option(fragment),
      pathStartsWithSlash
    )
  }

  def empty = apply()

  def apply(javaUri: java.net.URI): Uri = parse(javaUri.toASCIIString())
}
