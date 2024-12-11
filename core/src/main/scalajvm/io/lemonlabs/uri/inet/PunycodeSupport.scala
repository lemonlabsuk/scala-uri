package io.lemonlabs.uri.inet

import java.net.IDN

trait PunycodeSupport {
  def toPunycode(host: String): String =
    IDN.toASCII(host, IDN.ALLOW_UNASSIGNED)
}
