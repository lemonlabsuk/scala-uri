package io.lemonlabs.uri.decoding

trait UriDecoder extends Product with Serializable {
  def decodeBytes(data: String, charset: String): Array[Byte]

  def decode(u: String): String

  def decodeTuple(kv: (String, Option[String])) =
    decode(kv._1) -> kv._2.map(decode)
}
