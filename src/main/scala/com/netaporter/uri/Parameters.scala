package com.netaporter.uri

import com.netaporter.uri.encoding.UriEncoder
import com.netaporter.uri.Parameters._
import scala.Some
import scala.collection.GenTraversableOnce

/**
 * Trait use to represent a list of key value parameters, such as query string parameters and matrix parameters
 */
trait Parameters {
  type Self <: Parameters

  def separator: String

  def params: ParamSeq

  def withParams(params: ParamSeq): Self

  lazy val paramMap = params.groupBy(_._1)

  /**
   * Adds a new parameter key-value pair.
   *
   * @return A new instance with the new parameter added
   */
  def addParam(k: String, v: String): Self = addParam(k, Some(v))

  /**
   * Adds a new parameter key-value pair. If the value for the parameter is None, then this
   * parameter will be rendered without an = sign (use Some("") if this is not what you want).
   *
   * @return A new instance with the new parameter added
   */
  def addParam(k: String, v: Option[String]): Self =
    withParams(params :+ (k -> v.map(_.toString)))

  /**
   * Adds a new parameter key with no value. If the value for the parameter is None, then this
   * parameter will not be rendered
   *
   * @return A new instance with the new parameter added
   */
  def addParam(k: String): Self = addParam(k, None: Option[String])

  def addParams(kvs: ParamSeq) =
    withParams(params ++ kvs)

  def params(key: String): Seq[Option[String]] = params.collect {
    case (k, v) if k == key => v
  }

  def param(key: String) = params.collectFirst {
    case (k, Some(v)) if k == key => v
  }

  /**
   * Transforms each parameter by applying the specified Function
   *
   * @param f
   * @return
   */
  def mapParams(f: Param => Param) =
    withParams(params.map(f))

  /**
   * Transforms each parameter by applying the specified Function
   *
   * @param f A function that returns a collection of Parameters when applied to each parameter
   * @return
   */
  def flatMapParams(f: Param => GenTraversableOnce[Param]) =
    withParams(params.flatMap(f))

  /**
   * Transforms each parameter name by applying the specified Function
   *
   * @param f
   * @return
   */
  def mapParamNames(f: String => String) =
    withParams(params.map {
      case (n, v) => (f(n), v)
    })

  /**
   * Transforms each parameter value by applying the specified Function
   *
   * @param f
   * @return
   */
  def mapParamValues(f: String => String) =
    withParams(params.map {
      case (n, v) => (n, v map f)
    })

  /**
   * Filters out just the parameters for which the provided function holds true
   *
   * @param f
   * @return
   */
  def filterParams(f: Param => Boolean) =
    withParams(params.filter(f))

  /**
   * Filters out just the parameters for which the provided function holds true when applied to the parameter name
   *
   * @param f
   * @return
   */
  def filterParamsNames(f: String => Boolean) =
    withParams(params.filter {
      case (n, _) => f(n)
    })

  /**
   * Filters out just the parameters for which the provided function holds true when applied to the parameter value
   *
   * @param f
   * @return
   */
  def filterParamsValues(f: String => Boolean) =
    filterParamsOptions(ov => ov match {
      case Some(v) => f(v)
      case _ => false
    })

  /**
   * Filters out just the parameters for which the provided function holds true when applied to the parameter value
   *
   * @param f
   * @return
   */
  def filterParamsOptions(f: Option[String] => Boolean) =
    withParams(params.filter {
      case (_, v) => f(v)
    })

  /**
   * Replaces the all existing Query String parameters with the specified key with a single Query String parameter
   * with the specified value.
   *
   * @param k Key for the Query String parameter(s) to replace
   * @param vOpt value to replace with
   * @return A new QueryString with the result of the replace
   */
  def replaceAll(k: String, vOpt: Option[Any]): Self =
    withParams(params.filterNot(_._1 == k) :+ (k -> vOpt.map(_.toString)))

  /**
   * Removes all Query String parameters with the specified key
   * @param k Key for the Query String parameter(s) to remove
   * @return
   */
  def removeAll(k: String) =
    filterParamsNames(_ != k)

  def removeAll(a: Array[String]) =
    filterParamsNames(!a.contains(_))

  def removeAll() =
    withParams(Seq.empty)

  def paramsToString(e: UriEncoder, charset: String) =
    params.map(kv => {
      val (k, ov) = kv
      ov match {
        case Some(v) => e.encode(k, charset) + "=" + e.encode(v, charset)
        case None => e.encode(k, charset)
      }
    }).mkString(separator)
}

object Parameters {
  type Param = (String, Option[String])
  type ParamSeq = Seq[Param]
}