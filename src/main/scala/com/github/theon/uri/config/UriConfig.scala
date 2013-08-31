package com.github.theon.uri.config

import com.github.theon.uri.encoding.{NoopEncoder, UriEncoder, PercentEncoder}
import com.github.theon.uri.decoding.{PercentDecoder, UriDecoder}

/**
 * Date: 28/08/2013
 * Time: 21:31
 */
case class UriConfig(pathEncoder: UriEncoder,
                                   queryEncoder: UriEncoder,
                                   fragmentEncoder: UriEncoder,
                                   pathDecoder: UriDecoder,
                                   queryDecoder: UriDecoder,
                                   fragmentDecoder: UriDecoder,
                                   charset: String) {

  def withNoEncoding = copy(pathEncoder = NoopEncoder, queryEncoder = NoopEncoder, fragmentEncoder = NoopEncoder)

}

object UriConfig {

  val default = UriConfig()

  def apply(encoder: UriEncoder = PercentEncoder(),
            decoder: UriDecoder = PercentDecoder,
            charset: String = "UTF-8"): UriConfig =
    UriConfig(encoder, encoder, encoder, decoder, decoder, decoder, charset)
}
