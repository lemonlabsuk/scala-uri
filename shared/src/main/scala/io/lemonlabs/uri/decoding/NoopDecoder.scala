package io.lemonlabs.uri.decoding

case object NoopDecoder extends UriDecoder {
  def decode(s: String) = s
}
