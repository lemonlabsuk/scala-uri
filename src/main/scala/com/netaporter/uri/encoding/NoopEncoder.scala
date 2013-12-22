package com.netaporter.uri.encoding

/**
* Date: 28/08/2013
* Time: 21:15
*/
object NoopEncoder extends UriEncoder {
  def shouldEncode(ch: Char) = false
  def encodeChar(ch: Char) = ch.toString
}
