package io.lemonlabs.uri
import cats.{Eq, Order, Show}
import io.lemonlabs.uri.config.{All, ExcludeNones, UriConfig}
import io.lemonlabs.uri.parsing.UrlParser
import io.lemonlabs.uri.typesafe.{QueryKey, QueryKeyValue, QueryValue, TraversableParams}
import io.lemonlabs.uri.typesafe.TraversableParams.ops._
import io.lemonlabs.uri.typesafe.QueryKeyValue.ops._
import io.lemonlabs.uri.typesafe.QueryValue.ops._
import io.lemonlabs.uri.typesafe.QueryKey.ops._

import scala.util.Try

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
    * Pairs with values, such as `("param", Some("value"))`, represent query params with values, i.e `?param=value`
    *
    * By default, pairs without values, such as `("param", None)`, represent query params without values, i.e `?param`
    * Using a `UriConfig(renderQuery = ExcludeNones)`, will cause pairs with `None` values not to be rendered
    *
    * @return A new instance with the new parameter added
    */
  def addParam[K: QueryKey, V: QueryValue](k: K, v: V): QueryString =
    QueryString(params :+ (k.queryKey -> v.queryValue))

  /**
    * Adds a new Query String parameter key-value pair.
    *
    * Pairs with values, such as `("param", Some("value"))`, represent query params with values, i.e `?param=value`
    * Using a `UriConfig(renderQuery = ExcludeNones)`, will cause pairs with `None` values not to be rendered
    *
    * By default, pairs without values, such as `("param", None)`, represent query params without values, i.e `?param`
    */
  def addParam[KV: QueryKeyValue](kv: KV): QueryString =
    QueryString(params :+ (kv.queryKey -> kv.queryValue))

  /**
    * Adds all the specified key-value pairs as parameters to the query
    */
  def addParams(other: QueryString): QueryString =
    QueryString(params ++ other.params)

  /**
    * Adds all the specified key-value pairs as parameters to the query
    *
    * Pairs with values, such as `("param", Some("value"))`, represent query params with values, i.e `?param=value`
    *
    * By default, pairs without values, such as `("param", None)`, represent query params without values, i.e `?param`
    * Using a `UriConfig(renderQuery = ExcludeNones)`, will cause pairs with `None` values not to be rendered
    */
  def addParams[KV: QueryKeyValue](first: KV, second: KV, kvs: KV*): QueryString =
    addParams(first +: second +: kvs)

  /**
    * Adds all the specified key-value pairs as parameters to the query
    *
    * Pairs with values, such as `("param", Some("value"))`, represent query params with values, i.e `?param=value`
    *
    * By default, pairs without values, such as `("param", None)`, represent query params without values, i.e `?param`
    * Using a `UriConfig(renderQuery = ExcludeNones)`, will cause pairs with `None` values not to be rendered
    */
  def addParams[P: TraversableParams](kvs: P): QueryString =
    QueryString(params ++ kvs.toSeq)

  def params(key: String): Vector[Option[String]] =
    params.collect {
      case (k, v) if k == key => v
    }

  def param(key: String): Option[String] =
    params.collectFirst {
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
  def map[KV: QueryKeyValue](f: PartialFunction[(String, Option[String]), KV]): QueryString = {
    QueryString(params.iterator.map { kv =>
      if (f.isDefinedAt(kv)) f(kv).queryKeyValue else kv
    }.toVector)
  }

  /**
    * Transforms the Query String by applying the specified PartialFunction to each Query String Parameter
    *
    * Parameters not defined in the PartialFunction will be removed.
    *
    * @param f A function that returns a new Parameter when applied to each Parameter
    * @return
    */
  def collect[KV: QueryKeyValue](f: PartialFunction[(String, Option[String]), KV]): QueryString =
    QueryString(params.collect(f.andThen(_.queryKeyValue)))

  /**
    * Transforms each parameter by applying the specified Function
    *
    * @param f A function that returns a collection of Parameters when applied to each parameter
    * @return
    */
  def flatMap[A: TraversableParams](f: ((String, Option[String])) => A): QueryString =
    QueryString(params.flatMap(f.andThen(_.toSeq)))

  /**
    * Transforms each parameter name by applying the specified Function
    *
    * @param f
    * @return
    */
  def mapNames[K: QueryKey](f: String => K): QueryString =
    QueryString(params.map {
      case (n, v) => (f(n).queryKey, v)
    })

  /**
    * Transforms each parameter value by applying the specified Function
    *
    * @param f
    * @return
    */
  def mapValues[V: QueryValue](f: String => V): QueryString =
    QueryString(params.map {
      case (n, v) => (n, v.flatMap(f.andThen(_.queryValue)))
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
      case _            => false
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
  def replaceAll[K: QueryKey, V: QueryValue](k: K, v: V): QueryString =
    QueryString(params.filterNot(_._1 == k.queryKey) :+ (k.queryKey -> v.queryValue))

  /**
    * Removes all Query String parameters with the specified key
    * @param k Key for the Query String parameter(s) to remove
    * @return
    */
  def removeAll[K: QueryKey](k: K): QueryString =
    filterNames(_ != k.queryKey)

  /**
    * Removes all Query String parameters with a name in the specified list
    * @param first Name of a Query String parameter to remove
    * @param second Name of another Query String parameter to remove
    * @param rest Names of more Query String parameter(s) to remove
    * @return
    */
  def removeAll[K: QueryKey](first: K, second: K, rest: K*): QueryString =
    removeAll(Seq(first, second) ++ rest)

  /**
    * Removes all Query String parameters with a name in the specified list
    * @param k Names of Query String parameter(s) to remove
    * @return
    */
  def removeAll[K: QueryKey](k: Iterable[K]): QueryString =
    filterNames(name => !k.exists(_.queryKey == name))

  def isEmpty: Boolean = params.isEmpty
  def nonEmpty: Boolean = params.nonEmpty

  type ParamToString = PartialFunction[(String, Option[String]), String]

  private[uri] def toString(c: UriConfig): String = {
    val enc = c.queryEncoder
    val charset = c.charset

    val someToString: ParamToString = {
      case (k, Some(v)) => enc.encode(k, charset) + "=" + enc.encode(v, charset)
    }
    val paramToString: ParamToString = someToString orElse {
      case (k, None) => enc.encode(k, charset)
    }

    val paramsAsString = c.renderQuery match {
      case All          => params.map(paramToString)
      case ExcludeNones => params.collect(someToString)
    }

    if (paramsAsString.isEmpty) ""
    else paramsAsString.mkString("&")
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
  def fromPairs[KV: QueryKeyValue](first: KV, kv: KV*)(implicit
      config: UriConfig = UriConfig.default
  ): QueryString =
    fromTraversable(first +: kv)

  def fromTraversable[A: TraversableParams](params: A)(implicit config: UriConfig = UriConfig.default): QueryString =
    new QueryString(params.toVector)

  def empty(implicit config: UriConfig = UriConfig.default): QueryString =
    new QueryString(Vector.empty)

  def parseTry(s: CharSequence)(implicit config: UriConfig = UriConfig.default): Try[QueryString] =
    UrlParser.parseQuery(s.toString)

  def parseOption(s: CharSequence)(implicit config: UriConfig = UriConfig.default): Option[QueryString] =
    parseTry(s).toOption

  def parse(s: CharSequence)(implicit config: UriConfig = UriConfig.default): QueryString =
    parseTry(s).get

  implicit val eqQueryString: Eq[QueryString] = Eq.fromUniversalEquals
  implicit val showQueryString: Show[QueryString] = Show.fromToString
  implicit val orderQueryString: Order[QueryString] = Order.by(_.params)
}
