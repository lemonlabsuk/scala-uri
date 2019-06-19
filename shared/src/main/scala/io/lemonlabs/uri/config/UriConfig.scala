package io.lemonlabs.uri.config

import io.lemonlabs.uri.encoding.{NoopEncoder, PercentEncoder, UriEncoder}
import io.lemonlabs.uri.decoding.{PercentDecoder, UriDecoder}
import PercentEncoder._
import io.lemonlabs.uri.json.JsonSupportDefaults
import io.lemonlabs.uri.json.JsonSupport

case class UriConfig(userInfoEncoder: UriEncoder,
                     pathEncoder: UriEncoder,
                     queryEncoder: UriEncoder,
                     fragmentEncoder: UriEncoder,
                     userInfoDecoder: UriDecoder,
                     pathDecoder: UriDecoder,
                     queryDecoder: UriDecoder,
                     fragmentDecoder: UriDecoder,
                     charset: String,
                     renderQuery: RenderQuery,
                     jsonSupport: JsonSupport) {

  def withNoEncoding = copy(pathEncoder = NoopEncoder, queryEncoder = NoopEncoder, fragmentEncoder = NoopEncoder)

}

object UriConfig {

  val default = UriConfig(
    userInfoEncoder = PercentEncoder(USER_INFO_CHARS_TO_ENCODE),
    pathEncoder = PercentEncoder(PATH_CHARS_TO_ENCODE),
    queryEncoder = PercentEncoder(QUERY_CHARS_TO_ENCODE),
    fragmentEncoder = PercentEncoder(FRAGMENT_CHARS_TO_ENCODE),
    userInfoDecoder = PercentDecoder,
    pathDecoder = PercentDecoder,
    queryDecoder = PercentDecoder,
    fragmentDecoder = PercentDecoder,
    charset = "UTF-8",
    renderQuery = RenderQuery.default,
    jsonSupport = JsonSupportDefaults.default
  )

  /**
    * Probably more than you need to percent encode. Wherever possible try to use a tighter Set of characters
    * to encode depending on your use case
    */
  val conservative = UriConfig(PercentEncoder(), PercentDecoder)

  def apply(encoder: UriEncoder = PercentEncoder(),
            decoder: UriDecoder = PercentDecoder,
            charset: String = "UTF-8",
            renderQuery: RenderQuery = RenderQuery.default,
            jsonSupport: JsonSupport = JsonSupportDefaults.default): UriConfig =
    UriConfig(encoder, encoder, encoder, encoder, decoder, decoder, decoder, decoder, charset, renderQuery, jsonSupport)
}
