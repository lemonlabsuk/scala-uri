package io.lemonlabs.uri.typesafe.dsl

import io.lemonlabs.uri.Url
import io.lemonlabs.uri.typesafe.TraversableParams.ops._
import io.lemonlabs.uri.typesafe.QueryKeyValue.ops._
import io.lemonlabs.uri.typesafe.QueryValue.ops._
import io.lemonlabs.uri.typesafe.QueryKey.ops._
import io.lemonlabs.uri.typesafe.PathPart.ops._
import io.lemonlabs.uri.typesafe.Fragment.ops._
import io.lemonlabs.uri.typesafe.{Fragment, PathPart, QueryKey, QueryKeyValue, QueryValue, TraversableParams}

class TypesafeUrlDsl private[typesafe] (val url: Url) extends AnyVal {

  /**
    * Appends a path part to the path of this URI
    * @param a The path part
    * @return A new Uri with this path part appended
    */
  def /[A: PathPart](a: A): Url =
    url.addPathPart(a.path)

  /**
    * Adds a new Query String parameter key-value pair. If the value for the Query String parameter is None, then this
    * Query String parameter will be rendered without a value e.g. `?param` as opposed to `?param=value`
    * @param a Value which provides the key and the value for query parameter
    * @return A new Uri with the new Query String parameter
    */
  def ?[A: QueryKeyValue](a: A): Url =
    url.addParam(a.queryKey, a.queryValue)

  def ?[A: QueryKey, B: QueryValue](a: A, b: B): Url =
    url.addParam(a.queryKey, b.queryValue)

  /**
    * Adds a trailing forward slash to the path and a new Query String parameter key-value pair.
    * If the value for the Query String parameter is None, then this Query String parameter will
    * be rendered without a value e.g. `?param` as opposed to `?param=value`
    * @param a Value which provides the key and the value for query parameter
    * @return A new Uri with the new Query String parameter
    */
  def /?[A: QueryKeyValue](a: A): Url =
    /("").addParam(a.queryKey, a.queryValue)

  /**
    * Adds a trailing forward slash to the path and a new Query String parameter key-value pair.
    * If the value for the Query String parameter is None, then this Query String parameter will
    * be rendered without a value e.g. `?param` as opposed to `?param=value`
    * @param a Value which provides the key and the value for query parameter
    * @return A new Uri with the new Query String parameter
    */
  def /?[A: QueryKey, B: QueryValue](a: A, b: B): Url =
    /("").addParam(a.queryKey, b.queryValue)

  /**
    * Adds a new Query String parameter key-value pair. If the value for the Query String parameter is None, then this
    * Query String parameter will be rendered without a value e.g. `?param` as opposed to `?param=value`
    * @param a Value which provides the key and the value for query parameter
    * @return A new Uri with the new Query String parameter
    */
  def &[A: QueryKeyValue](a: A): Url =
    url.addParam(a.queryKey, a.queryValue)

  /**
    * Adds a new Query String parameter key-value pair. If the value for the Query String parameter is None, then this
    * Query String parameter will be rendered without a value e.g. `?param` as opposed to `?param=value`
    * @param a Value which provides the key and the value for query parameter
    * @return A new Uri with the new Query String parameter
    */
  def &[A: QueryKey, B: QueryValue](a: A, b: B): Url =
    url.addParam(a.queryKey, b.queryValue)

  /**
    * Maybe adds a new Query String parameter key-value pair.
    * If the value for the Query String parameter is None, then this Query String parameter will not be added,
    * otherwise it will be added
    * @param a Value which provides the key and the value for query parameter
    * @return A new Uri with the new Query String parameter
    */
  def &&[A: QueryKeyValue](a: A): Url = a.queryValue.map(_ => &(a)).getOrElse(url)

  /**
    * Maybe adds a new Query String parameter key-value pair.
    * If the value for the Query String parameter is None, then this Query String parameter will not be added,
    * otherwise it will be added
    * @param a Value which provides the key and the value for query parameter
    * @return A new Uri with the new Query String parameter
    */
  def &&[A: QueryKey, B: QueryValue](a: A, b: B): Url = b.queryValue.map(_ => &(a, b)).getOrElse(url)

  /**
    * Adds a fragment to the end of the uri
    * @param a Value representing the fragment
    * @return A new Uri with this fragment
    */
  def `#`[A: Fragment](a: A): Url =
    url.withFragment(a.fragment)

  def withParams[A: TraversableParams](params: A): Url =
    url.addParamsOptionValues(params.toSeq)

  def withParams[A: QueryKeyValue](param1: A, param2: A, params: A*): Url =
    withParams((Seq(param1, param2) ++ params).toList)

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

  def /(other: Url): Url = merge(other)
  def ?(other: Url): Url = merge(other)
  def `#`(other: Url): Url = merge(other)
  def &(other: Url): Url = merge(other)
}
