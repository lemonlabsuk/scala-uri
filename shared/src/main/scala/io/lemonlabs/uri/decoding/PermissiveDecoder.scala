package io.lemonlabs.uri.decoding

case class PermissiveDecoder(child: UriDecoder) extends UriDecoder {
  def decode(s: String) = {
    try {
      child.decode(s)
    } catch {
      case _: Throwable => s
    }
  }

  def decodeBytes(s: String, charset: String): Array[Byte] = {
    try {
      child.decodeBytes(s, charset)
    } catch {
      case _: Throwable => s.getBytes(charset)
    }
  }
}

object PermissivePercentDecoder extends PermissiveDecoder(PercentDecoder)
