package io.lemonlabs.uri.inet

import java.net.IDN

trait PunycodeSupport {
  def toPunycode(host: String) =
    IDN.toASCII(host)
}
