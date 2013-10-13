package com.github.theon.uri

import com.github.theon.uri.config.UriConfig
import Parameters._

/**
 * Date: 28/08/2013
 * Time: 21:21
 */
trait PathPart extends Any {

  type Self <: PathPart

  /**
   * The non-parameter part of this pathPart
   *
   * @return
   */
  def part: String

  /**
   * Adds a matrix parameter to the end of this path part
   *
   * @param kv
   */
  def addParam(kv: Param): PathPart

  def params: ParamSeq

  def partToString(c: UriConfig): String

  def map(f: String=>String): Self
}

case class StringPathPart(part: String) extends AnyVal with PathPart {
  type Self = StringPathPart

  def params = Vector.empty

  def addParam(kv: Param) =
    MatrixParams(part, Vector(kv))

  def partToString(c: UriConfig) =
    c.pathEncoder.encode(part, c.charset)

  def map(f: String=>String) =
    StringPathPart(f(part))
}

case class MatrixParams(part: String, params: ParamSeq) extends PathPart with Parameters[MatrixParams] {
  type Self = MatrixParams

  def separator = ";"
  def withParams(params: ParamSeq) = copy(params = params)

  def partToString(c: UriConfig) =
    c.pathEncoder.encode(part, c.charset) + ";" + paramsToString(c.pathEncoder, c.charset)

  def addParam(kv: Param) =
    copy(params = params :+ kv)

  def map(f: String=>String) =
    MatrixParams(f(part), params)

  def mapParams(f: Param=>Param) =
    MatrixParams(part, params.map(f))
}

object PathPart {
  def apply(path: String, matrixParams: ParamSeq = Seq.empty) =
    if(matrixParams.isEmpty) new StringPathPart(path) else MatrixParams(path, matrixParams)
}