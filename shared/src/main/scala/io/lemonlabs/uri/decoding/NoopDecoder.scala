package io.lemonlabs.uri.decoding

object NoopDecoder extends UriDecoder {
  def decode(s: String) = s
}
