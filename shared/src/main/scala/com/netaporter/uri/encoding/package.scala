package com.netaporter.uri

import com.netaporter.uri.config.UriConfig

/**
 * Date: 28/08/2013
 * Time: 21:08
 */
package object encoding {
  val percentEncode = PercentEncoder()
  def percentEncode(chars: Char*) = PercentEncoder(chars.toSet)

  def encodeCharAs(c: Char, as: String) = EncodeCharAs(c, as)
  val spaceAsPlus = EncodeCharAs(' ', "+")
}
