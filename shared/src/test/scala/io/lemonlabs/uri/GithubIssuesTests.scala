package io.lemonlabs.uri

import io.lemonlabs.uri.config.UriConfig
import io.lemonlabs.uri.decoding.{PercentDecoder, UriDecodeException}
import io.lemonlabs.uri.encoding.NoopEncoder
import io.lemonlabs.uri.parsing.UriParsingException
import org.scalatest.OptionValues
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

/**
  * Test Suite to ensure that bugs raised by awesome github peeps NEVER come back
  *
  * These bugs were raised on thr github issues page https://github.com/lemonlabsuk/scala-uri/issues
  */
class GithubIssuesTests extends AnyFlatSpec with Matchers with OptionValues {
  "Github Issue #1" should "throw UriDecodeException when url contains invalid percent encoding" in {
    Vector("/?x=%3", "/%3", "/?a=%3&b=whatever", "/?%3=okay").foreach { part =>
      withClue(part) {
        an[UriDecodeException] should be thrownBy Url.parse(part)
        Url.parseOption(part) should equal(None)
        Url.parseTry(part).isSuccess should equal(false)
      }
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

  "Github Issue #20" should "support URLs with long numbers" in {
    val url = Url.parse("https://12345678987654321.example.com/")
    url.hostOption should equal(Some(DomainName("12345678987654321.example.com")))
  }

  "Github Issue #21" should "support really long port numbers" in {
    val url = Url.parse("soundcloud://sounds:78237871")
    url.port should equal(Some(78237871))
  }

  "Github Issue #30" should "handle correctly percent encoded URLs when ignoreInvalidPercentEncoding=true" in {
    implicit val c = UriConfig(
      decoder = PercentDecoder(ignoreInvalidPercentEncoding = true)
    )
    val url = Url.parse("http://example.com/path%20with%20space")
    url.toString should equal("http://example.com/path%20with%20space")
  }

  "Github Issue #98" should "not allow spaces in hosts" in {
    Seq(" " -> " ", "\n" -> "\\n", "\t" -> "\\t", "\r" -> "\\r").foreach {
      case (ch, chToString) =>
        val e = the[UriParsingException] thrownBy AbsoluteUrl.parse(s"https://www.goog${ch}le.com?q=i+am+invalid")
        e.getMessage should startWith(s"Invalid Url could not be parsed. Invalid input '$chToString'")
    }
  }
}
