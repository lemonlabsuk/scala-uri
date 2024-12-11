package io.lemonlabs.uri.decoding

case object NoopDecoder extends UriDecoder {
  def decode(s: String) = s
  def decodeBytes(data: String, charset: String): Array[Byte] = data.getBytes(charset)
}
