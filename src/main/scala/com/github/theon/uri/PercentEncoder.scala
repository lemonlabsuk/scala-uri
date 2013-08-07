package com.github.theon.uri

import java.nio.charset.Charset
import PercentEncoderDefaults._

object Encoders {
  val PercentEncoder = new PercentEncoder(DEFAULT_CHARS_TO_ENCODE)
  val EncodeSpaceAsPlus = EncodeCharAs(' ', "+")

  object NoopEncoder extends UriEncoder {
    def shouldEncode(ch: Char) = false
    def encode(ch: Char) = ch.toString
  }

  def encode(s: String, encoder: UriEncoder) = {
    val chars = s.getBytes("UTF-8").map(_.toChar)

    val encChars = chars.flatMap(ch => {
      if (encoder.shouldEncode(ch)) {
         encoder.encode(ch).getBytes("UTF-8")
      } else {
        Array(ch.toByte)
      }
    })

    new String(encChars, "UTF-8")
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