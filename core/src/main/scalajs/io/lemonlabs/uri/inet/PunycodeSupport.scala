package io.lemonlabs.uri.inet

import org.scalajs.dom.URL

trait PunycodeSupport {
  def toPunycode(host: String): String = {
    // the URL class IDN formats and escapes the host
    new URL(s"http://$host").host
  }
}
