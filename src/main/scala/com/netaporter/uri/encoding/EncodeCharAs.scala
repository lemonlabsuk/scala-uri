package com.netaporter.uri.encoding

/**
 * Date: 28/08/2013
 * Time: 21:07
 */
case class EncodeCharAs(ch: Char, as: String) extends UriEncoder {
  def shouldEncode(x: Char) = x == ch
  def encodeChar(x: Char) = as
}
