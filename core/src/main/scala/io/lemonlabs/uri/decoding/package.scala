package io.lemonlabs.uri

package object decoding {
  val percentDecode: PercentDecoder = PercentDecoder

  def decodeCharAs(c: Char, as: String) = DecodeCharAs(c, as)
  val plusAsSpace = DecodeCharAs('+', " ")
}
