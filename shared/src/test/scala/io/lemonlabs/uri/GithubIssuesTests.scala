package io.lemonlabs.uri

import io.lemonlabs.uri.Path.SlashTermination.RemoveForAll
import io.lemonlabs.uri.config.UriConfig
import io.lemonlabs.uri.decoding.{PercentDecoder, UriDecodeException}
import io.lemonlabs.uri.encoding.NoopEncoder
import io.lemonlabs.uri.parsing.UriParsingException
import org.scalatest.OptionValues
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import scala.util.Success

/** Test Suite to ensure that bugs raised by awesome github peeps NEVER come back
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
    Seq(" " -> " ", "\n" -> "\\n", "\t" -> "\\t", "\r" -> "\\r").foreach { case (ch, chToString) =>
      val e = the[UriParsingException] thrownBy AbsoluteUrl.parse(s"https://www.goog${ch}le.com?q=i+am+invalid")
      e.getMessage should startWith(s"Invalid Url could not be parsed.")
    }
  }

  "Github Issue #192" should "not allow forward slashes in passwords unless percent encoded" in {
    val url = Url.parse("http://example.com:123/@path:")
    url.hostOption should equal(Some(DomainName("example.com")))
    url.port should equal(Some(123))
    url.path.toString() should equal("/@path:")
  }

  "Github Issue #193" should "parse a IPv6 with a IPv4 as the least significant 32bits" in {
    val url = Url.parse("s://@[f4E0:F65b:734E:dc04:026b:8629:226.252.129.66]:3298571?")
    url.hostOption should equal(Some(IpV6("f4e0", "f65b", "734e", "dc04", "026b", "8629", "e2fc", "8142")))
  }

  "Github Issue #195" should "parse a seven segment elided IPv6" in {
    val url = Url.parse("https://[E873:4eC5:eBc9:9e97:6BcE:998C:95AD::]/")
    url.hostOption should equal(Some(IpV6("e873", "4ec5", "ebc9", "9e97", "6bce", "998c", "95ad", "0")))
  }

  "Github Issue #204" should "parse a domain name host with IPv4 prefix" in {
    Host.parse("1.2.3.4.blah") should equal(DomainName("1.2.3.4.blah"))
  }

  "Github Issue #304" should "remove empty path parts and remove trailing slashes in the path" in {
    import io.lemonlabs.uri.typesafe.dsl._
    val url1 = Url.parse("http://example.com/") / "bar"
    url1.removeEmptyPathParts().toString should equal("http://example.com/bar")
    url1.normalize(removeEmptyPathParts = true).toString should equal("http://example.com/bar")

    val url2 = Url.parse("https://example.com/")
    val normalized2 = url2.normalize(removeEmptyPathParts = true, slashTermination = RemoveForAll)
    normalized2.toString should equal("https://example.com")
  }

  "Github Issue #318" should "not include private domains from the public suffix list" in {
    Url.parse("http://475952.temp-dns.com/recipes/").subdomain should equal(Some("475952"))
    Url.parse("https://allrecipes-01.web.app/").subdomain should equal(Some("allrecipes-01"))
  }

  "Github Issue #368" should "allow parsing of an empty query" in {
    QueryString.parseTry("") should equal(Success(QueryString.empty))
  }

  "Github Issue #399" should "allow parsing paths with colons" in {
    Url.parseTry("/this:1/does/not") should equal(
      Success(RelativeUrl(AbsolutePath.fromParts("this:1", "does", "not"), QueryString.empty, None))
    )
  }

  it should "allow parsing paths with square brackets" in {
    Url.parseTry("/this[1]/does/not") should equal(
      Success(RelativeUrl(AbsolutePath.fromParts("this[1]", "does", "not"), QueryString.empty, None))
    )
  }

  "Github Issue #424" should "turn emojis into punycode" in {
    Url.parseTry("http://👸🏽.cf").map(_.toStringPunycode) should equal(
      Success("http://xn--on8hvh.cf")
    )
  }

  "Github Issue #429" should "not allow forward slashes in host" in {
    Url.parseOption("https://\\") should equal(None)
  }
}
