package com.netaporter.uri

import com.netaporter.uri.config.UriConfig
import com.netaporter.uri.parsing.UrlParser

trait Path {
  def config: UriConfig
  def parts: Vector[String]
  private[uri] def toString(config: UriConfig): String

  /**
    * Returns the path with no encoders taking place (e.g. non ASCII characters will not be percent encoded)
    * @return String containing the raw path for this Uri
    */
  def toStringRaw: String =
    toString(config.withNoEncoding)

  override def toString: String =
    toString(config)
}

object Path {
  def parse(s: CharSequence)(implicit config: UriConfig = UriConfig.default): Path =
    UrlParser.parsePath(s.toString)
}