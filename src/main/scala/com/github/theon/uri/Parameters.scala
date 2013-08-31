package com.github.theon.uri

import com.github.theon.uri.encoding.UriEncoder
import Parameters.ParamSeq
/**
 * Trait use to represent a list of key value parameters, such as query string parameters and matrix parameters
 */
trait Parameters[+Self] {
  this: Self =>

  def separator: String
  def params: ParamSeq
  def withParams(params: ParamSeq): Self

  lazy val paramMap = params.groupBy(_._1)

  /**
   * Adds a new parameter key-value pair. If the value for the parameter is None, then this
   * parameter will not be rendered
   *
   * @return A new instance with the new parameter added
   */
  def addParam(k: String, v: String) =
    withParams(params :+ (k -> v.toString))

  def addParams(kvs: ParamSeq) =
    withParams(params ++ kvs)

  def params(key: String): Seq[String] = params.collect {
    case (k, v) if k == key => v
  }

  def param(key: String) = params.collectFirst {
    case (k, v) if k == key => v
  }

  /**
   * Replaces the all existing Query String parameters with the specified key with a single Query String parameter
   * with the specified value. If the value passed in is None, then all Query String parameters with the specified key
   * are removed
   *
   * @param k Key for the Query String parameter(s) to replace
   * @param vOpt value to replace with
   * @return A new QueryString with the result of the replace
   */
  def replaceAll(k: String, vOpt: Option[Any]) = {
    vOpt match {
      case Some(v) => withParams(params.filterNot(_._1 == k) :+ (k -> v.toString))
      case None => removeAll(k)
    }
  }

  /**
   * Removes all Query String parameters with the specified key
   * @param k Key for the Query String parameter(s) to remove
   * @return
   */
  def removeAll(k: String) = {
    withParams(params.filterNot(_._1 == k))
  }

  def paramsToString(e: UriEncoder, charset: String) =
    params.map(kv => {
      val (k,v) = kv
      e.encode(k, charset) + "=" + e.encode(v, charset)
    }).mkString(separator)
}

object Parameters {
  type Param = (String,String)
  type ParamSeq = Seq[Param]
}