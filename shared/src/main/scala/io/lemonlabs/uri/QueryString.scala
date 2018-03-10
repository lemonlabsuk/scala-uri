package io.lemonlabs.uri

import io.lemonlabs.uri.config.UriConfig
import io.lemonlabs.uri.parsing.UrlParser

import scala.collection.{GenTraversable, GenTraversableOnce}

case class QueryString(params: Vector[(String, Option[String])])(implicit config: UriConfig = UriConfig.default) {

  lazy val paramMap: Map[String, Vector[String]] = params.foldLeft(Map.empty[String, Vector[String]]) {
    case (m, (k, Some(v))) =>
      val values = m.getOrElse(k, Vector.empty)
      m + (k -> (values :+ v))

    // For query parameters with no value (e.g. /blah?q), Put at explicit Nil into the Map
    // If there is already an entry in the Map from a previous parameter with the same name, maintain it
    case (m, (k, None)) =>
      val values = m.getOrElse(k, Vector.empty)
      m + (k -> values)
  }

  /**
    * Adds a new parameter key-value pair.
    *
    * @return A new instance with the new parameter added
    */
  def addParam(k: String, v: String): QueryString =
    addParam(k, Some(v))

  /**
    * Pairs with values, such as `("param", Some("value"))`, represent query params with values, i.e `?param=value`
    * Pairs without values, such as `("param", None)`, represent query params without values, i.e `?param`
    *
    * @return A new instance with the new parameter added
    */
  def addParam(k: String, v: Option[String]) =
    QueryString(params :+ (k -> v))

  /**
    * Adds a new parameter key with no value, e.g. `?param`
    *
    * @return A new instance with the new parameter added
    */
  def addParam(k: String): QueryString =
    addParam(k, None)

/**
  * Adds a new Query String parameter key-value pair.
  */
  def addParam(kv: (String, String)): QueryString =
    QueryString(params :+ (kv._1 -> Some(kv._2)))

  /**
    * Adds a new Query String parameter key-value pair.
    *
    * Pairs with values, such as `("param", Some("value"))`, represent query params with values, i.e `?param=value`
    * Pairs without values, such as `("param", None)`, represent query params without values, i.e `?param`
    */
  def addParamOptionValue(kv: (String, Option[String])): QueryString =
    QueryString(params :+ kv)

  /**
    * Adds all the specified key-value pairs as parameters to the query
    */
  def addParams(other: QueryString): QueryString =
    QueryString(params ++ other.params)

  /**
    * Adds all the specified key-value pairs as parameters to the query
    */
  def addParams(kvs: (String, String)*): QueryString =
    addParams(kvs)

  /**
    * Adds all the specified key-value pairs as parameters to the query
    */
  def addParams(kvs: GenTraversable[(String, String)]): QueryString =
    addParamsOptionValues(kvs.map { case (k,v) => (k, Some(v)) })

  /**
    * Adds all the specified key-value pairs as parameters to the query
    *
    * Pairs with values, such as `("param", Some("value"))`, represent query params with values, i.e `?param=value`
    * Pairs without values, such as `("param", None)`, represent query params without values, i.e `?param`
    */
  def addParamsOptionValues(kvs: GenTraversable[(String, Option[String])]): QueryString =
    QueryString(params ++ kvs)

  def params(key: String): Vector[Option[String]] = params.collect {
    case (k, v) if k == key => v
  }

  def param(key: String): Option[String] = params.collectFirst {
    case (k, Some(v)) if k == key => v
  }

  /**
    * Transforms the Query String by applying the specified PartialFunction to each Query String Parameter
    *
    * Parameters not defined in the PartialFunction will be left as-is.
    *
    * @param f A function that returns a new Parameter when applied to each Parameter
    * @return
    */
  def map(f: PartialFunction[(String, Option[String]), (String, Option[String])]): QueryString = {
    QueryString(params.map { kv =>
      if(f.isDefinedAt(kv)) f(kv) else kv
    })
  }

  /**
    * Transforms the Query String by applying the specified PartialFunction to each Query String Parameter
    *
    * Parameters not defined in the PartialFunction will be removed.
    *
    * @param f A function that returns a new Parameter when applied to each Parameter
    * @return
    */
  def collect(f: PartialFunction[(String, Option[String]), (String, Option[String])]): QueryString =
    QueryString(params.collect(f))

  /**
    * Transforms each parameter by applying the specified Function
    *
    * @param f A function that returns a collection of Parameters when applied to each parameter
    * @return
    */
  def flatMap(f: ((String, Option[String])) => GenTraversableOnce[(String, Option[String])]): QueryString =
    QueryString(params.flatMap(f))

  /**
    * Transforms each parameter name by applying the specified Function
    *
    * @param f
    * @return
    */
  def mapNames(f: String => String): QueryString =
    QueryString(params.map {
      case (n, v) => (f(n), v)
    })

  /**
    * Transforms each parameter value by applying the specified Function
    *
    * @param f
    * @return
    */
  def mapValues(f: String => String): QueryString =
    QueryString(params.map {
      case (n, v) => (n, v map f)
    })

  /**
    * Filters out just the parameters for which the provided function holds true
    *
    * @param f
    * @return
    */
  def filter(f: ((String, Option[String])) => Boolean): QueryString =
    QueryString(params.filter(f))

  /**
    * Filters out just the parameters for which the provided function holds true when applied to the parameter name
    *
    * @param f
    * @return
    */
  def filterNames(f: String => Boolean): QueryString =
    QueryString(params.filter {
      case (n, _) => f(n)
    })

  /**
    * Filters out just the parameters for which the provided function holds true when applied to the parameter value
    *
    * @param f
    * @return
    */
  def filterValues(f: String => Boolean): QueryString =
    QueryString(params.filter {
      case (_, Some(v)) => f(v)
      case _ => false
    })

  /**
    * Filters out just the parameters for which the provided function holds true when applied to the parameter value
    *
    * @param f
    * @return
    */
  def filterOptionValues(f: Option[String] => Boolean): QueryString =
    QueryString(params.filter {
      case (_, v) => f(v)
    })

  /**
    * Replaces the all existing Query String parameters with the specified key with a single Query String parameter
    * with the specified value.
    *
    * If the value passed in is None, then all Query String parameters with the specified key are replaces with a
    * valueless query param. E.g. `replaceParams("q", None)` would turn `?q=1&q=2` into `?q`
    *
    * @param k Key for the Query String parameter(s) to replace
    * @param v value to replace with
    * @return A new QueryString with the result of the replace
    */
  def replaceAll(k: String, v: Option[String]): QueryString =
    QueryString(params.filterNot(_._1 == k) :+ (k -> v))

  /**
    * Replaces the all existing Query String parameters with the specified key with a single Query String parameter
    * with the specified value.
    *
    * @param k Key for the Query String parameter(s) to replace
    * @param v value to replace with
    * @return A new QueryString with the result of the replace
    */
  def replaceAll(k: String, v: String): QueryString =
    replaceAll(k, Some(v))

  /**
    * Removes all Query String parameters with the specified key
    * @param k Key for the Query String parameter(s) to remove
    * @return
    */
  def removeAll(k: String): QueryString =
    filterNames(_ != k)

  /**
    * Removes all Query String parameters with a name in the specified list
    * @param k Names of Query String parameter(s) to remove
    * @return
    */
  def removeAll(k: String*): QueryString =
    removeAll(k)

  /**
    * Removes all Query String parameters with a name in the specified list
    * @param k Names of Query String parameter(s) to remove
    * @return
    */
  def removeAll(k: GenTraversableOnce[String]): QueryString =
    filterNames(name => !k.exists(_ == name))

  def isEmpty: Boolean = params.isEmpty
  def nonEmpty: Boolean = params.nonEmpty

  private[uri] def toString(c: UriConfig): String =
    if(params.isEmpty) ""
    else {
      val enc = c.queryEncoder
      val charset = c.charset
      val paramsToString = params.map {
        case (k, Some(v)) => enc.encode(k, charset) + "=" + enc.encode(v, charset)
        case (k, None   ) => enc.encode(k, charset)
      }
      "?" + paramsToString.mkString("&")
    }

  /**
    * Returns the query string with no encoding taking place (e.g. non ASCII characters will not be percent encoded)
    * @return String containing the raw query string for this Uri
    */
  def toStringRaw: String =
    toString(config.withNoEncoding)

  override def toString: String =
    toString(config)
}

object QueryString {
  def fromPairOptions(params: (String, Option[String])*)(implicit config: UriConfig = UriConfig.default): QueryString =
    new QueryString(params.toVector)

  def fromPairs(params: (String, String)*)(implicit config: UriConfig = UriConfig.default): QueryString =
    fromTraversable(params)

  def fromTraversable(params: GenTraversableOnce[(String, String)])(implicit config: UriConfig = UriConfig.default): QueryString =
    new QueryString(params.toVector.map {
      case (k, v) => (k, Some(v))
    })

  def empty(implicit config: UriConfig = UriConfig.default): QueryString =
    new QueryString(Vector.empty)

  def parse(s: CharSequence)(implicit config: UriConfig = UriConfig.default): QueryString =
    UrlParser.parseQuery(s.toString)
}