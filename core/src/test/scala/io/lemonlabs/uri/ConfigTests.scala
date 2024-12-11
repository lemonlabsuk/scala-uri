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

  val percentEncoderI: PercentEncoder = PercentEncoder(Set('i'))
  val configPercentEncodeI: UriConfig = UriConfig(encoder = percentEncoderI)

  "withConfig" should "replace the config for a RelativeUrl" in {
    val url = RelativeUrl.parse("/index.html").withConfig(configPercentEncodeI)
    url.toString should equal("/%69ndex.html")
  }

  it should "replace the config for a AbsoluteUrl" in {
    val url = AbsoluteUrl.parse("https://localhost/index.html").withConfig(configPercentEncodeI)
    url.toString should equal("https://localhost/%69ndex.html")
  }

  it should "replace the config for a ProtocolRelativeUrl" in {
    val url = ProtocolRelativeUrl.parse("//localhost/index.html").withConfig(configPercentEncodeI)
    url.toString should equal("//localhost/%69ndex.html")
  }

  it should "replace the config for a SimpleUrlWithoutAuthority" in {
    val url = SimpleUrlWithoutAuthority.parse("mailto:index@example.com").withConfig(configPercentEncodeI)
    url.toString should equal("mailto:%69ndex@example.com")
  }

  it should "replace the config for a DataUrl" in {
    val url = DataUrl.parse("data:,index").withConfig(configPercentEncodeI)
    url.toString should equal("data:,%69ndex")
  }

  it should "replace the config for a ScpLikeUrl" in {
    val url = ScpLikeUrl.parse("data:,index").withConfig(configPercentEncodeI)
    url.config should equal(configPercentEncodeI)
  }

  it should "replace the config for a Urn" in {
    val url = Urn.parse("urn:html:index").withConfig(configPercentEncodeI)
    url.toString should equal("urn:html:%69ndex")
  }

  it should "replace the config for a Authority" in {
    val authority = Authority.parse("index@localhost").withConfig(configPercentEncodeI)
    authority.toString should equal("%69ndex@localhost")
  }

  it should "replace the config for a DomainName" in {
    val host = DomainName.parse("localhost").withConfig(configPercentEncodeI)
    host.conf should equal(configPercentEncodeI)
  }

  it should "replace the config for a IpV4" in {
    val host = IpV4.parse("127.0.0.1").withConfig(configPercentEncodeI)
    host.conf should equal(configPercentEncodeI)
  }

  it should "replace the config for a IpV6" in {
    val host = IpV6.parse("[::1]").withConfig(configPercentEncodeI)
    host.conf should equal(configPercentEncodeI)
  }

  it should "do nothing for EmptyPath" in {
    val path = EmptyPath.withConfig(configPercentEncodeI)
    path should equal(EmptyPath)
  }

  it should "replace the config for a AbsolutePath" in {
    val path = AbsolutePath.fromParts("index").withConfig(configPercentEncodeI)
    path.toString should equal("/%69ndex")
  }

  it should "replace the config for a RootlessPath" in {
    val path = RootlessPath.fromParts("index").withConfig(configPercentEncodeI)
    path.toString should equal("%69ndex")
  }

  it should "replace the config for a UrnPath" in {
    val path = UrnPath.parse("html:index").withConfig(configPercentEncodeI)
    path.toString should equal("html:%69ndex")
  }

  it should "replace the config for a QueryString" in {
    val qs = QueryString.parse("?index&html").withConfig(configPercentEncodeI)
    qs.toString should equal("%69ndex&html")
  }
}
