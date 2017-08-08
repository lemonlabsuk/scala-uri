package com.netaporter.uri

import com.netaporter.uri.config.UriConfig
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

  def partToString(c: UriConfig): String

  def map(f: String=>String): Self
}

case class StringPathPart(part: String) extends AnyVal with PathPart {

  type Self = StringPathPart

  def partToString(c: UriConfig) =
    c.pathEncoder.encode(part, c.charset)

  def map(f: String=>String) =
    StringPathPart(f(part))
}

object PathPart {
  def apply(path: String) = StringPathPart(path)
}
