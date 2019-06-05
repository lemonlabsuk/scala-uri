package io.lemonlabs.uri.decoding

trait UriDecoder extends Product with Serializable {
  def decode(u: String): String

  def decodeTuple(kv: (String, Option[String])) =
    decode(kv._1) -> kv._2.map(decode)
}
