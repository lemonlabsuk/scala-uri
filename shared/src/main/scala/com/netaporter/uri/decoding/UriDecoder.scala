package com.netaporter.uri.decoding

trait UriDecoder {
  def decode(u: String): String

  def decodeTuple(kv: (String, Option[String])) =
    decode(kv._1) -> kv._2.map(decode)
}
