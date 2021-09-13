package io.lemonlabs.uri

import io.lemonlabs.uri.config.{RenderQuery, UriConfig}
import io.lemonlabs.uri.decoding.{plusAsSpace, PercentDecoder}
import io.lemonlabs.uri.encoding.PercentEncoder
import io.lemonlabs.uri.encoding.PercentEncoder.{
  FRAGMENT_CHARS_TO_ENCODE,
  PATH_CHARS_TO_ENCODE,
  QUERY_CHARS_TO_ENCODE,
  USER_INFO_CHARS_TO_ENCODE
}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class ConfigTests extends AnyFlatSpec with Matchers {
  "Config constructor without defaultPorts" should "use the default defaultPorts" in {
    val conf = UriConfig(
      userInfoEncoder = PercentEncoder(USER_INFO_CHARS_TO_ENCODE),
      pathEncoder = PercentEncoder(PATH_CHARS_TO_ENCODE),
      queryEncoder = PercentEncoder(QUERY_CHARS_TO_ENCODE),
      fragmentEncoder = PercentEncoder(FRAGMENT_CHARS_TO_ENCODE),
      userInfoDecoder = PercentDecoder,
      pathDecoder = PercentDecoder,
      queryDecoder = plusAsSpace + PercentDecoder,
      fragmentDecoder = PercentDecoder,
      charset = "UTF-8",
      renderQuery = RenderQuery.default
    )

    conf.defaultPorts should equal(
      Map(
        "ftp" -> 21,
        "http" -> 80,
        "https" -> 443,
        "ws" -> 80,
        "wss" -> 80
      )
    )
  }

  "Config apply without defaultPorts" should "use the default defaultPorts" in {
    val conf = UriConfig(
      userInfoEncoder = PercentEncoder(USER_INFO_CHARS_TO_ENCODE),
      pathEncoder = PercentEncoder(PATH_CHARS_TO_ENCODE),
      queryEncoder = PercentEncoder(QUERY_CHARS_TO_ENCODE),
      fragmentEncoder = PercentEncoder(FRAGMENT_CHARS_TO_ENCODE),
      userInfoDecoder = PercentDecoder,
      pathDecoder = PercentDecoder,
      queryDecoder = plusAsSpace + PercentDecoder,
      fragmentDecoder = PercentDecoder,
      charset = "UTF-8",
      renderQuery = RenderQuery.default
    )

    conf.defaultPorts should equal(
      Map(
        "ftp" -> 21,
        "http" -> 80,
        "https" -> 443,
        "ws" -> 80,
        "wss" -> 80
      )
    )
  }

  "Config withDefaultPorts" should "update the default ports" in {
    val testDefaultPorts = Map("http" -> 80)
    val conf = UriConfig.default.withDefaultPorts(testDefaultPorts)
    conf.defaultPorts should equal(testDefaultPorts)
  }

  "Config copy without defaultPorts" should "maintain the same default ports" in {
    val testDefaultPorts = Map("http" -> 80)
    val conf = UriConfig.default.withDefaultPorts(testDefaultPorts).copy(charset = "UTF-8")
    conf.defaultPorts should equal(testDefaultPorts)
  }
}
