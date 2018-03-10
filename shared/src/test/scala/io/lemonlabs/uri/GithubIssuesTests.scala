package io.lemonlabs.uri

import io.lemonlabs.uri.config.UriConfig
import io.lemonlabs.uri.decoding.{PercentDecoder, UriDecodeException}
import io.lemonlabs.uri.encoding.NoopEncoder
import org.scalatest.{FlatSpec, Matchers, OptionValues}

/**
  * Test Suite to ensure that bugs raised by awesome github peeps NEVER come back
  *
  * These bugs were raised on thr github issues page https://github.com/lemonlabsuk/scala-uri/issues
  */
class GithubIssuesTests extends FlatSpec with Matchers with OptionValues {

  "Github Issue #1" should "throw UriDecodeException when url contains invalid percent encoding" in {
    Vector("/?x=%3", "/%3", "/?a=%3&b=whatever", "/?%3=okay").foreach { part =>
      an[UriDecodeException] should be thrownBy Url.parse(part)
    }
  }

  it should "leave invalid percent encoded entities as-is when ignoreInvalidPercentEncoding=true" in {
    implicit val c = UriConfig(
      encoder = NoopEncoder,
      decoder = PercentDecoder(ignoreInvalidPercentEncoding = true)
    )
    Vector("/?x=%3", "/%3", "/?a=%3&b=whatever", "/?%3=okay").foreach { part =>
      Url.parse(part).toString shouldBe part
    }
  }
}
