package io.lemonlabs.uri.encoding

trait UriEncoder extends Product with Serializable {
  def shouldEncode(ch: Char): Boolean
  def encodeChar(ch: Char): String

  def encode(s: String, charset: String) = {
    val chars = s.getBytes(charset).map(_.toChar)

    val encChars = chars.flatMap(ch => {
      if (shouldEncode(ch)) {
        encodeChar(ch).getBytes(charset)
      } else {
        Array(ch.toByte)
      }
    })

    new String(encChars, charset)
  }

  def +(other: UriEncoder) = ChainedUriEncoder(other :: this :: Nil)
}
