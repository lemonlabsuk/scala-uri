package com.netaporter.uri.decoding

import com.netaporter.uri.Uri

/**
 * Date: 28/08/2013
 * Time: 20:58
 */
object NoopDecoder extends UriDecoder {
  def decode(s: String) = s
}
