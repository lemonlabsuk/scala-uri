package io.lemonlabs.uri.dsl

import io.lemonlabs.uri.Url
import io.lemonlabs.uri.parsing.UrlParser

/**
  * Value class to add DSL functionality to Urls
  */
@deprecated(
  "Please migrate to the typesafe DSL in the io.lemonlabs.uri.typesafe.dsl package. See https://github.com/lemonlabsuk/scala-uri#typesafe-url-builder-dsl for more information",
  "2.0.0"
)
class UrlDsl(val url: Url) extends AnyVal {
  import url.config

  private def anyToQueryValue(any: Any): Option[String] = any match {
    case v: Option[_] => v.map(_.toString)
    case _            => Some(any.toString)
  }

  /**
    * Adds a new Query String parameter key-value pair. If the value for the Query String parameter is None, then this
    * Query String parameter will be rendered without a value e.g. `?param` as opposed to `?param=value`
    * @param kv Tuple2 representing the query string parameter
    * @return A new Uri with the new Query String parameter
    */
  @deprecated(
    "Please migrate to the typesafe DSL in the io.lemonlabs.uri.typesafe.dsl package. See https://github.com/lemonlabsuk/scala-uri#typesafe-url-builder-dsl for more information",
    "2.0.0"
  )
  def ?(kv: (String, Any)): Url =
    url.addParam(kv._1, anyToQueryValue(kv._2))

  /**
    * Adds a new Query String. The specified String is parsed as a Query String param.
    * @return A new Uri with the new Query String parameter
    */
  @deprecated(
    "Please migrate to the typesafe DSL in the io.lemonlabs.uri.typesafe.dsl package. See https://github.com/lemonlabsuk/scala-uri#typesafe-url-builder-dsl for more information",
    "2.0.0"
  )
  def ?(kv: String): Url =
    url.addParamOptionValue(UrlParser.parseQueryParam(kv).get)

  /**
    * Adds a trailing forward slash to the path and a new Query String parameter key-value pair.
    * If the value for the Query String parameter is None, then this Query String parameter will
    * be rendered without a value e.g. `?param` as opposed to `?param=value`
    * @param kv Tuple2 representing the query string parameter
    * @return A new Uri with the new Query String parameter
    */
  @deprecated(
    "Please migrate to the typesafe DSL in the io.lemonlabs.uri.typesafe.dsl package. See https://github.com/lemonlabsuk/scala-uri#typesafe-url-builder-dsl for more information",
    "2.0.0"
  )
  def /?(kv: (String, Any)): Url =
    /("").addParam(kv._1, anyToQueryValue(kv._2))

  /**
    * Maybe adds a new Query String parameter key-value pair.
    * If the value for the Query String parameter is None, then this Query String parameter will not be added,
    * otherwise it will be added
    * @param kv Tuple2 representing the query string parameter
    * @return A new Uri with the new Query String parameter
    */
  @deprecated(
    "Please migrate to the typesafe DSL in the io.lemonlabs.uri.typesafe.dsl package. See https://github.com/lemonlabsuk/scala-uri#typesafe-url-builder-dsl for more information",
    "2.0.0"
  )
  def &&(kv: (String, Any)): Url = kv match {
    case (k, None) => url
    case (k, v)    => &(k, v)
  }

  /**
    * Adds a new Query String parameter key-value pair. If the value for the Query String parameter is None, then this
    * Query String parameter will be rendered without a value e.g. `?param` as opposed to `?param=value`
    * @param kv Tuple2 representing the query string parameter
    * @return A new Uri with the new Query String parameter
    */
  @deprecated(
    "Please migrate to the typesafe DSL in the io.lemonlabs.uri.typesafe.dsl package. See https://github.com/lemonlabsuk/scala-uri#typesafe-url-builder-dsl for more information",
    "2.0.0"
  )
  def &(kv: (String, Any)): Url =
    url.addParam(kv._1, anyToQueryValue(kv._2))

  /**
    * Adds a new Query String. The specified String is parsed as a Query String param.
    * @return A new Uri with the new Query String parameter
    */
  @deprecated(
    "Please migrate to the typesafe DSL in the io.lemonlabs.uri.typesafe.dsl package. See https://github.com/lemonlabsuk/scala-uri#typesafe-url-builder-dsl for more information",
    "2.0.0"
  )
  def &(kv: String): Url =
    ?(kv)

  /**
    * Adds all the specified key-value pairs as parameters to the query
    *
    * @param kvs A list of key-value pairs to add as query parameters
    * @return A new Url with the new Query String parameters
    */
  @deprecated(
    "Please migrate to the typesafe DSL in the io.lemonlabs.uri.typesafe.dsl package. See https://github.com/lemonlabsuk/scala-uri#typesafe-url-builder-dsl for more information",
    "2.0.0"
  )
  def addParams(kvs: Iterable[(String, Any)]): Url =
    url.addParamsOptionValues(kvs.map { case (k, v) => (k, anyToQueryValue(v)) })

  /**
    * Adds a fragment to the end of the uri
    * @param fragment String representing the fragment
    * @return A new Uri with this fragment
    */
  @deprecated(
    "Please migrate to the typesafe DSL in the io.lemonlabs.uri.typesafe.dsl package. See https://github.com/lemonlabsuk/scala-uri#typesafe-url-builder-dsl for more information",
    "2.0.0"
  )
  def `#`(fragment: String): Url =
    url.withFragment(fragment)

  /**
    * Appends a path part to the path of this URI
    * @param pp The path part
    * @return A new Uri with this path part appended
    */
  @deprecated(
    "Please migrate to the typesafe DSL in the io.lemonlabs.uri.typesafe.dsl package. See https://github.com/lemonlabsuk/scala-uri#typesafe-url-builder-dsl for more information",
    "2.0.0"
  )
  def /(pp: String): Url =
    url.addPathPart(pp)

  /**
    * Operator precedence in Scala will mean that our DSL will not always be executed left to right.
    *
    * For the operators this DSL cares about, the order will be
    *
    * (all letters)
    * &
    * :
    * /
    * `#` ?
    *
    * (see Scala Reference - 6.12.3 Infix Operations: http://www.scala-lang.org/docu/files/ScalaReference.pdf)
    *
    * To handle cases where the right hard part of the DSL is executed first, we turn that into a Uri, and merge
    * it with the left had side. It is assumed the right hand Uri is generated from this DSL only to add path
    * parts, query parameters or to overwrite the fragment
    *
    * @param other A Uri generated by more DSL to the right of us
    * @return A Uri with the right hand DSL merged into us
    */
  private def merge(other: Url): Url =
    url
      .withFragment(other.fragment.orElse(url.fragment))
      .withQueryString(url.query.addParams(other.query))
      .withPath(url.path.addParts(other.path.parts))

  @deprecated(
    "Please migrate to the typesafe DSL in the io.lemonlabs.uri.typesafe.dsl package. See https://github.com/lemonlabsuk/scala-uri#typesafe-url-builder-dsl for more information",
    "2.0.0"
  )
  def /(other: Url): Url = merge(other)

  @deprecated(
    "Please migrate to the typesafe DSL in the io.lemonlabs.uri.typesafe.dsl package. See https://github.com/lemonlabsuk/scala-uri#typesafe-url-builder-dsl for more information",
    "2.0.0"
  )
  def ?(other: Url): Url = merge(other)

  @deprecated(
    "Please migrate to the typesafe DSL in the io.lemonlabs.uri.typesafe.dsl package. See https://github.com/lemonlabsuk/scala-uri#typesafe-url-builder-dsl for more information",
    "2.0.0"
  )
  def `#`(other: Url): Url = merge(other)

  @deprecated(
    "Please migrate to the typesafe DSL in the io.lemonlabs.uri.typesafe.dsl package. See https://github.com/lemonlabsuk/scala-uri#typesafe-url-builder-dsl for more information",
    "2.0.0"
  )
  def &(other: Url): Url = merge(other)
}
