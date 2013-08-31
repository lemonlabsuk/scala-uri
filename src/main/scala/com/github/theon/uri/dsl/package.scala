package com.github.theon.uri

import com.github.theon.uri.encoding.{ChainedUriEncoder, UriEncoder}
import com.github.theon.uri.config.UriConfig

/**
 * Date: 23/08/2013
 * Time: 09:10
 */
package object dsl {
  implicit def uriToUriOps(uri: Uri) = new UriDsl(uri)

  implicit def encoderToChainedEncoder(enc: UriEncoder) = ChainedUriEncoder(enc :: Nil)

  implicit def stringToUri(s: String)(implicit c: UriConfig = UriConfig.default) = Uri.parse(s)(c)
  implicit def stringToUriDsl(s: String)(implicit c: UriConfig = UriConfig.default) = new UriDsl(stringToUri(s)(c))

  implicit def uriToString(uri: Uri)(implicit c: UriConfig = UriConfig.default): String = uri.toString(c)
}
