package io.lemonlabs.uri.encoding

case object NoopEncoder extends UriEncoder {
  def shouldEncode(ch: Char) = false
  def encodeChar(ch: Char) = ch.toString
}
