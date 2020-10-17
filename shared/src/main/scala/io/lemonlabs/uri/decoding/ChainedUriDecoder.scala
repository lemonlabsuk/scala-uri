package io.lemonlabs.uri.decoding

case class ChainedUriDecoder(decoders: Seq[UriDecoder]) extends UriDecoder {

  override def decodeBytes(data: String, charset: String): Array[Byte] = {
    val asStr = decoders.foldLeft(data) { (str, decoder) =>
      new String(decoder.decodeBytes(str, charset), charset)
    }
    asStr.getBytes(charset)
  }

  override def decode(data: String): String =
    decoders.foldLeft(data) { (str, decoder) =>
      decoder.decode(str)
    }

  override def +(encoder: UriDecoder): ChainedUriDecoder = copy(decoders = decoders :+ encoder)
}
