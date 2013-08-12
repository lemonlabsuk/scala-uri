package com.github.theon.uri

import PercentEncoderDefaults._

object Encoders {
  val PercentEncoder = new PercentEncoder(DEFAULT_CHARS_TO_ENCODE)
  def PercentEncoder(chars: Char*) = new PercentEncoder(chars.toSet)

  val EncodeSpaceAsPlus = EncodeCharAs(' ', "+")

  object NoopEncoder extends UriEncoder {
    def shouldEncode(ch: Char) = false
    def encode(ch: Char) = ch.toString
  }

  def encode(s: String, encoder: UriEncoder, enc: String = "UTF-8") = {
    val chars = s.getBytes(enc).map(_.toChar)

    val encChars = chars.flatMap(ch => {
      if (encoder.shouldEncode(ch)) {
         encoder.encode(ch).getBytes(enc)
      } else {
        Array(ch.toByte)
      }
    })

    new String(encChars, enc)
  }
}

object PercentEncoderDefaults {
  val DEFAULT_CHARS_TO_ENCODE = Set (
    ' ', '%', '$', '&', '+', ',', '/', ':', ';', '=', '?', '@', '<', '>', '[', ']', '(', ')', '#', '%', '!', '\'', '*',
    '{', '}', '\n', '\r', '^', '`', '|', '~', '\\'
  )
}

class PercentEncoder(val charsToEncode: Set[Char]) extends UriEncoder {

  def shouldEncode(ch: Char) = {
    !ascii(ch) || charsToEncode.contains(ch)
  }

  def encode(ch: Char) = "%" + toHex(ch)
  def toHex(ch: Char) = "%04x".format(ch.toInt).substring(2).toUpperCase

  def ascii(ch: Char) = ch > 0 && ch < 128

  def --(chars: Char*) = new PercentEncoder(charsToEncode -- chars)
  def ++(chars: Char*) = new PercentEncoder(charsToEncode ++ chars)
}

case class EncodeCharAs(ch: Char,as: String) extends UriEncoder {
  def shouldEncode(x: Char) = x == ch
  def encode(x: Char) = as
}

case class ChainedUriEncoder(encoders: List[UriEncoder]) extends UriEncoder {
  def shouldEncode(ch: Char) = findFirstEncoder(ch).isDefined
  def encode(ch: Char) = findFirstEncoder(ch).get.encode(ch)

  def findFirstEncoder(ch: Char) = {
    encoders.find(_.shouldEncode(ch))
  }

  def +(encoder: UriEncoder) = copy(encoders = encoder :: encoders)
}

trait UriEncoder {
  def shouldEncode(ch: Char): Boolean
  def encode(ch: Char): String
}