package io.lemonlabs.uri.decoding

import io.lemonlabs.uri.Uri

class PermissiveDecoder(child: UriDecoder) extends UriDecoder {
  def decode(s: String) = {
    try {
      child.decode(s)
    } catch {
      case _: Throwable => s
    }
  }
}

object PermissivePercentDecoder extends PermissiveDecoder(PercentDecoder)