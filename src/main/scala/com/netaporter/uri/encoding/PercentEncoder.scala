package com.netaporter.uri.encoding

import PercentEncoder._

case class PercentEncoder(charsToEncode: Set[Char] = DEFAULT_CHARS_TO_ENCODE) extends UriEncoder {

  def shouldEncode(ch: Char) = {
    !ascii(ch) || charsToEncode.contains(ch)
  }

  def encodeChar(ch: Char) = "%" + toHex(ch)
  def toHex(ch: Char) = "%04x".format(ch.toInt).substring(2).toUpperCase

  def ascii(ch: Char) = ch > 0 && ch < 128

  def --(chars: Char*) = new PercentEncoder(charsToEncode -- chars)
  def ++(chars: Char*) = new PercentEncoder(charsToEncode ++ chars)
}

object PercentEncoder {
  val DEFAULT_CHARS_TO_ENCODE = Set (
    ' ', '%', '$', '&', '+', ',', '/', ':', ';', '=', '?', '@', '<', '>', '[', ']', '(', ')', '#', '%',
    '!', '\'', '*', '{', '}', '\n', '\r', '^', '`', '|', '~', '\\'
  )
}