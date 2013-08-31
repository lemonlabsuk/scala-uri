package com.github.theon.uri.decoding

import com.github.theon.uri.Parameters._
/**
 * Date: 28/08/2013
 * Time: 21:01
 */
trait UriDecoder {
  def decode(u: String): String

  def decodeTuple(kv: Param) =
    decode(kv._1) -> decode(kv._2)
}
