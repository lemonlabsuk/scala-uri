package io.lemonlabs.uri.config

import io.lemonlabs.uri.decoding.{plusAsSpace, PercentDecoder, UriDecoder}
import io.lemonlabs.uri.encoding.PercentEncoder._
import io.lemonlabs.uri.encoding.{spaceAsPlus, NoopEncoder, PercentEncoder, UriEncoder}

case class UriConfig(userInfoEncoder: UriEncoder,
                     pathEncoder: UriEncoder,
                     queryEncoder: UriEncoder,
                     fragmentEncoder: UriEncoder,
                     userInfoDecoder: UriDecoder,
                     pathDecoder: UriDecoder,
                     queryDecoder: UriDecoder,
                     fragmentDecoder: UriDecoder,
                     charset: String,
                     renderQuery: RenderQuery
) {
  def withNoEncoding = copy(pathEncoder = NoopEncoder, queryEncoder = NoopEncoder, fragmentEncoder = NoopEncoder)
}

object UriConfig {
  val default = UriConfig(
    userInfoEncoder = PercentEncoder(USER_INFO_CHARS_TO_ENCODE),
    pathEncoder = PercentEncoder(PATH_CHARS_TO_ENCODE),
    queryEncoder = PercentEncoder(QUERY_CHARS_TO_ENCODE) + spaceAsPlus,
    fragmentEncoder = PercentEncoder(FRAGMENT_CHARS_TO_ENCODE),
    userInfoDecoder = PercentDecoder,
    pathDecoder = PercentDecoder,
    queryDecoder = plusAsSpace + PercentDecoder,
    fragmentDecoder = PercentDecoder,
    charset = "UTF-8",
    renderQuery = RenderQuery.default
  )

  /** Probably more than you need to percent encode. Wherever possible try to use a tighter Set of characters
    * to encode depending on your use case
    */
  val conservative = default.copy(
    userInfoEncoder = PercentEncoder(),
    pathEncoder = PercentEncoder(),
    queryEncoder = PercentEncoder() + spaceAsPlus,
    fragmentEncoder = PercentEncoder()
  )

  def apply(encoder: UriEncoder = PercentEncoder(),
            decoder: UriDecoder = PercentDecoder,
            charset: String = "UTF-8",
            renderQuery: RenderQuery = RenderQuery.default
  ): UriConfig =
    UriConfig(encoder, encoder, encoder, encoder, decoder, decoder, decoder, decoder, charset, renderQuery)
}
