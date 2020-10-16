package io.lemonlabs.uri.encoding

trait UriEncoder extends Product with Serializable {
  def shouldEncode(ch: Char): Boolean
  def encodeChar(ch: Char): String

  def encode(s: String, charset: String): String = {
    val bytes = s.getBytes(charset)
    encode(bytes, charset)
  }

  def encode(bytes: Array[Byte], charset: String): String = {
    val chars = bytes.map(_.toChar)
    val encChars = chars.flatMap(ch => {
      if (shouldEncode(ch)) {
        encodeChar(ch).getBytes(charset)
      } else {
        Array(ch.toByte)
      }
    })

    new String(encChars, charset)
  }

  def +(other: UriEncoder) = ChainedUriEncoder(this :: other :: Nil)
}
