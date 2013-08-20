package com.github.theon.uri

import com.github.theon.uri.Uri._
import com.github.theon.uri.Encoders.{NoopEncoder, PercentEncoder, encode}

case class Uri (
  protocol: Option[String],
  user: Option[String],
  password:  Option[String],
  host: Option[String],
  port: Option[Int],
  pathParts: List[String],
  query: Querystring,
  fragment: Option[String] = None
) {

  //TODO: Tidy to h.split('.').toVector when 2.9.2 support is dropped
  lazy val hostParts: Seq[String] =
    host.map(h => Vector(h.split('.'): _*)).getOrElse(Vector.empty)

  def this(scheme: Option[String], host: Option[String], path: String, query: Querystring = Querystring()) = {
    this(scheme, None, None, host, None, path.dropWhile(_ == '/').split('/').toList, query, None)
  }

  def subdomain = hostParts.headOption

  /**
   * Adds a new Query String parameter key-value pair. If the value for the Query String parmeter is None, then this
   * Query String parameter will not be rendered in calls to toString or toStringRaw
   * @param kv Tuple2 representing the querystring parameter
   * @return A new Uri with the new Query String parameter
   */
  def param(kv: (String, Any)) = {
    val (k,v) = kv
    v match {
      case valueOpt: Option[_] =>
        copy(query = query & (k, valueOpt))
      case _ =>
        copy(query = query & (k, Some(v)))
    }
  }

  def params(kvs: Seq[(String,Any)]) = kvs.foldLeft(this) {
    (uri, param) => uri ? param
  }

  def scheme = protocol

  /**
   * Copies this Uri but with the scheme set as the given value.
   *
   * @param scheme the new scheme to set
   * @return a new Uri with the specified scheme
   */
  def scheme(scheme: String): Uri = copy(protocol = Option(scheme))

  /**
   * Copies this Uri but with the host set as the given value.
   *
   * @param host the new host to set
   * @return a new Uri with the specified host
   */
  def host(host: String): Uri = copy(host = Option(host))

  /**
   * Copies this Uri but with the user set as the given value.
   *
   * @param user the new user to set
   * @return a new Uri with the specified user
   */
  def user(user: String): Uri = copy(user = Option(user))

  /**
   * Copies this Uri but with the password set as the given value.
   *
   * @param password the new password to set
   * @return a new Uri with the specified password
   */
  def password(password: String): Uri = copy(password = Option(password))

  /**
   * Copies this Uri but with the port set as the given value.
   *
   * @param port the new host to set
   * @return a new Uri with the specified port
   */
  def port(port: Int): Uri = copy(port = Option(port))

  /**
   * Adds a new Query String parameter key-value pair. If the value for the Query String parameter is None, then this
   * Query String parameter will not be rendered in calls to toString or toStringRaw
   * @param kv Tuple2 representing the query string parameter
   * @return A new Uri with the new Query String parameter
   */
  def ?(kv: (String, Any)) = param(kv)

  /**
   * Adds a new Query String parameter key-value pair. If the value for the Query String parameter is None, then this
   * Query String parameter will not be rendered in calls to toString or toStringRaw
   * @param kv Tuple2 representing the query string parameter
   * @return A new Uri with the new Query String parameter
   */
  def &(kv: (String, Any)) = param(kv)

  /**
   * Adds a fragment to the end of the uri
   * @param fragment String representing the fragment
   * @return A new Uri with this fragment
   */
  def `#`(fragment: String) = copy(fragment = Some(fragment))

  /**
   * Returns the path with no encoding taking place (e.g. non ASCII characters will not be percent encoded)
   * @return String containing the raw path for this Uri
   */
  def pathRaw = path(NoopEncoder)

  /**
   * Returns the encoded path. By default non ASCII characters in the path are percent encoded.
   * @return String containing the path for this Uri
   */
  def path(implicit e: Enc = PercentEncoder): String =
    "/" + pathParts.map(encode(_, e)).mkString("/")

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
        copy(query = query.replaceParams(k, valueOpt))
      case _ =>
        copy(query = query.replaceParams(k, Some(v)))
    }
  }

  /**
   * Removes all Query String parameters with the specified key
   * @param k Key for the Query String parameter(s) to remove
   * @return
   */
  def removeParams(k: String) = {
    copy(query = query.removeParams(k))
  }

  /**
   * Replaces the all existing Query String parameters with the specified key with a single Query String parameter
   * with the specified value.
   *
   * @deprecated Use replaceParams() instead
   */
  @Deprecated
  def replace(k: String, v: String) = {
    copy(query = query.replaceParams(k, Some(v)))
  }

  override def toString = toString(PercentEncoder)
  def toString(implicit e: Enc = PercentEncoder): String = {
    //If there is no scheme, we use protocol relative
    val schemeStr = scheme.map(_ + "://").getOrElse("//")
    val userInfo = user.map(_ + password.map(":" + _).getOrElse("") + "@").getOrElse("")
    host.map(schemeStr + userInfo + _).getOrElse("") +
      port.map(":" + _).getOrElse("") +
      path(e) +
      query.toString("?", e) +
      fragment.map(f => "#" + encode(f, e)).getOrElse("")
  }

  /**
   * Returns the string representation of this Uri with no encoding taking place
   * (e.g. non ASCII characters will not be percent encoded)
   * @return String containing this Uri in it's raw form
   */
  def toStringRaw(): String = toString(NoopEncoder)
}

case class Querystring(params: Map[String,List[String]] = Map()) {

  /**
   * Replaces the all existing Query String parameters with the specified key with a single Query String parameter
   * with the specified value.
   *
   * @deprecated Use replaceParams() instead
   */
  @Deprecated
  def replace(k: String, v: String) = {
    copy(params = params + (k -> List(v)))
  }

  /**
   * Replaces the all existing Query String parameters with the specified key with a single Query String parameter
   * with the specified value. If the value passed in is None, then all Query String parameters with the specified key
   * are removed
   *
   * @param k Key for the Query String parameter(s) to replace
   * @param v value to replace with
   * @return A new QueryString with the result of the replace
   */
  def replaceParams(k: String, v: Option[Any]) = {
    v match {
      case Some(v) => copy(params = params + (k -> List(v.toString)))
      case None => removeParams(k)
    }
  }

  /**
   * Removes all Query String parameters with the specified key
   * @param k Key for the Query String parameter(s) to remove
   * @return
   */
  def removeParams(k: String) = {
    copy(params = params.filterNot(_._1 == k))
  }

  /**
   * Adds a new Query String parameter key-value pair. If the value for the Query String parmeter is None, then this
   * Query String parameter will not be rendered in calls to toString or toStringRaw
   * @param kv Tuple2 representing the querystring parameter
   * @return A new Query String with the new Query String parameter
   */
  def &(kv: (String, Option[Any])) = {
    val (k,vOpt) = kv
    vOpt match {
      case Some(v) => {
        val values = params.getOrElse(k, List())
        copy(params = params + (k -> (v.toString :: values)))
      }
      case _ => this
    }
  }

  override def toString() = toString(PercentEncoder)
  def toString(e: Enc): String = {
    params.flatMap(kv => {
      val (k,v) = kv
      v.map(encode(k, e) + "=" + encode(_, e))
    }).mkString("&")
  }

  /**
   * Returns the string representation of this QueryString with no encoding taking place
   * (e.g. non ASCII characters will not be percent encoded)
   * @return String containing this QueryString in it's raw form
   */
  def toStringRaw(): String = toString(NoopEncoder)

  def toString(prefix: String, e: Enc): String = {
    if(params.isEmpty) {
      ""
    } else {
      prefix + toString(e)
    }
  }
}

object Uri {
  type Enc = UriEncoder

  implicit def stringToUri(s: String)(implicit d: UriDecoder = PercentDecoder) = parseUri(s)
  implicit def uriToString(uri: Uri)(implicit e: UriEncoder = PercentEncoder): String = uri.toString(e)
  implicit def encoderToChainerEncoder(enc: UriEncoder) = ChainedUriEncoder(enc :: Nil)

  def parseUri(s: CharSequence)(implicit d: UriDecoder = PercentDecoder): Uri =
    UriParser.parse(s.toString, d)

  def apply(scheme: String, host: String, path: String): Uri =
    new Uri(Some(scheme), Some(host), path)

  def apply(scheme: String, host: String, path: String, query: Querystring): Uri =
    new Uri(Some(scheme), Some(host), path, query)

  def apply(scheme: Option[String], host: String, path: String): Uri =
    new Uri(scheme, Some(host), path)

  def apply(scheme: Option[String], host: String, path: String, query: Querystring): Uri =
    new Uri(scheme, Some(host), path, query)

  def apply(path: String): Uri =
    new Uri(None, None, path)

  def apply(path: String, query: Querystring): Uri =
    new Uri(None, None, path, query)
}
