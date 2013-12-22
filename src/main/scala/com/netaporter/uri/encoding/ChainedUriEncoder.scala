package com.netaporter.uri.encoding

/**
 * Date: 28/08/2013
 * Time: 21:07
 */
case class ChainedUriEncoder(encoders: Seq[UriEncoder]) extends UriEncoder {
  def shouldEncode(ch: Char) = findFirstEncoder(ch).isDefined
  def encodeChar(ch: Char) = findFirstEncoder(ch).getOrElse(NoopEncoder).encodeChar(ch)

  def findFirstEncoder(ch: Char) = {
    encoders.find(_.shouldEncode(ch))
  }

  def +(encoder: UriEncoder) = copy(encoders = encoder +: encoders)
}
