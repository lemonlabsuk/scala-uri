package com.github.theon.uri.decoding

import com.github.theon.uri.Uri

/**
 * Date: 28/08/2013
 * Time: 20:58
 */
object NoopDecoder extends UriDecoder {
  def decode(s: String) = s
}
