package io.lemonlabs.uri.decoding

case class DecodeCharAs(ch: Char, as: String) extends UriDecoder {
  override def decodeBytes(data: String, charset: String): Array[Byte] =
    decode(data).getBytes(charset)

  override def decode(data: String): String =
    data.replace(ch.toString, as)
}
