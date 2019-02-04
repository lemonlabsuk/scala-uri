package io.lemonlabs.uri.inet

import io.lemonlabs.uri.NotImplementedForScalaJsError

trait PunycodeSupport {
  def toPunycode(host: String) =
    throw NotImplementedForScalaJsError
}
