package com.netaporter.uri.decoding

object NoopDecoder extends UriDecoder {
  def decode(s: String) = s
}
