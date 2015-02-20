package com.netaporter.uri.encoding

import PercentEncoder._

case class PercentEncoder(charsToEncode: Set[Char] = DEFAULT_CHARS_TO_ENCODE) extends UriEncoder {

  def shouldEncode(ch: Char) = {
    !ascii(ch) || charsToEncode.contains(ch)
  }

  def encodeChar(ch: Char) = "%" + toHex(ch)
  def toHex(ch: Char) = "%04x".format(ch.toInt).substring(2).toUpperCase

  /**
   * Determines if this character is in the ASCII range (excluding control characters)
   */
  def ascii(ch: Char) = ch > 31 && ch < 127

  def --(chars: Char*) = new PercentEncoder(charsToEncode -- chars)
  def ++(chars: Char*) = new PercentEncoder(charsToEncode ++ chars)
}

object PercentEncoder {
  val PATH_CHARS_TO_ENCODE = Set (
    ' ', '%', '?', '<', '>', '[', ']', '#', '%', '{', '}', '^', '`', '|'
  )

  val QUERY_CHARS_TO_ENCODE = Set (
    '&', ' ', '%', '<', '>', '[', ']', '#', '%', '{', '}', '^', '`', '|', '\\', '+', '='
  )

  val GEN_DELIMS = Set(':', '/', '?',  '#', '[', ']', '@')
  val SUB_DELIMS  = Set('!', '$', '&', '\'', '(', ')', '*', '+', ',', ';', '=')
  val RESERVED = GEN_DELIMS ++ SUB_DELIMS

  val EXCLUDED = Set('"') // RFC 2396 section 2.4.3

  /**
   * Probably more than you need to percent encode. Wherever possible try to use a tighter Set of characters
   * to encode depending on your use case
   */
  val DEFAULT_CHARS_TO_ENCODE = RESERVED ++ PATH_CHARS_TO_ENCODE ++ QUERY_CHARS_TO_ENCODE ++ EXCLUDED
}