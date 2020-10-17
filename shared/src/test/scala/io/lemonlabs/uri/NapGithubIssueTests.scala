package io.lemonlabs.uri

import io.lemonlabs.uri.config.UriConfig
import io.lemonlabs.uri.decoding.PermissivePercentDecoder
import io.lemonlabs.uri.encoding.PercentEncoder
import io.lemonlabs.uri.parsing.UriParsingException
import org.scalatest.OptionValues
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

/** Test Suite to ensure that bugs raised by awesome github peeps NEVER come back
  *
  * These bugs were raised at the old github page https://github.com/net-a-porter/scala-uri/issues
  */
class NapGithubIssueTests extends AnyFlatSpec with Matchers with OptionValues {
  "Github Issue #2" should "now be fixed. Pluses in querystrings should be encoded when using the conservative encoder" in {
    val uri = Url.parse("http://theon.github.com/").addParam("+", "+")
    uri.toString(UriConfig.conservative) should equal("http://theon.github.com/?%2B=%2B")
  }

  "Github Issue #4" should "now be fixed. Port numbers should be rendered by toString" in {
    val uri = Url.parse("http://theon.github.com:8080/test?p=1")
    uri.toString should equal("http://theon.github.com:8080/test?p=1")
  }

  "Github Issue #5" should "now be fixed. The characters {} should now be percent encoded" in {
    val uri = Url.parse("http://theon.github.com/{}?{}={}")
    uri.toString should equal("http://theon.github.com/%7B%7D?%7B%7D=%7B%7D")
  }

  "Github Issue #7" should "now be fixed. Calling uri.toString() (with parentheses) should now behave the same as uri.toString " in {
    val uri = Url.parse("/blah?blah=blah")
    uri.toString() should equal("/blah?blah=blah")
  }

  "Github Issue #8" should "now be fixed. Parsed relative uris should have no scheme" in {
    val uri = Url.parse("abc")

    uri.schemeOption should equal(None)
    uri.hostOption should equal(None)
    uri.path.toString() should equal("abc")
  }

  "Github Issue #15" should "now be fixed. Empty Query String values are parsed" in {
    val uri = Url.parse("http://localhost:8080/ping?oi=TscV16GUGtlU&ppc=&bpc=")

    uri.schemeOption should equal(Some("http"))
    uri.hostOption should equal(Some(DomainName("localhost")))
    uri.port.value should equal(8080)
    uri.path.toString() should equal("/ping")
    uri.query.params("oi") should equal(Vector(Some("TscV16GUGtlU")))
    uri.query.params("ppc") should equal(Vector(Some("")))
    uri.query.params("bpc") should equal(Vector(Some("")))
  }

  "Github Issue #12" should "now be fixed. Parsing URIs parse percent escapes" in {
    val source = Url(
      scheme = "http",
      host = "xn--ls8h.example.net",
      path = "/path with spaces",
      query = QueryString.fromPairs("a b" -> "c d")
    )
    val parsed = Url.parse(source.toString)
    parsed should equal(source)
  }

  "Github Issue #19" should "now be fixed" in {
    val uri = Url.parse("/coldplay.com?singer=chris%26will")
    uri.toString should equal("/coldplay.com?singer=chris%26will")
  }

  "Github Issue #26" should "now be fixed" in {
    val uri = "http://lesswrong.com/index.php?query=abc%yum&john=hello"
    val conf = UriConfig(decoder = PermissivePercentDecoder)
    val u = Url.parse(uri)(conf)
    u.query.param("query") should equal(Some("abc%yum"))
  }

  "Github Issue #37" should "now be fixed" in {
    val uri = Url.parse("http://test.com:8080/something")
    uri.toString should equal("http://test.com:8080/something")
  }

  "Github Issue #55" should "now be fixed" in {
    val uri = Url.parse(
      "http://localhost:9002/iefjiefjief-efefeffe-fefefee/toto?access_token=ijifjijef-fekieifj-fefoejfoef&gquery=filter(time_before_closing%3C=45)"
    )
    uri.query.param("gquery") should equal(Some("filter(time_before_closing<=45)"))
    uri.toString should equal(
      "http://localhost:9002/iefjiefjief-efefeffe-fefefee/toto?access_token=ijifjijef-fekieifj-fefoejfoef&gquery=filter(time_before_closing%3C%3D45)"
    )
  }

  "Github Issue #56" should "now be fixed" in {
    val badPort = "http://test.net:8o8o"

    val ex = the[UriParsingException] thrownBy Url.parse(badPort)
    ex.getMessage should startWith("Invalid URL could not be parsed")

    Url.parseTry(badPort).isSuccess should equal(false)
    Url.parseOption(badPort) should equal(None)
  }

  "Github Issue #65 example 1" should "now be fixed" in {
    val uri = Url.parse("http://localhost:9000/?foo=test&&bar=test")
    uri.toString should equal("http://localhost:9000/?foo=test&&bar=test")
  }

  "Github Issue #65 example 2" should "now be fixed" in {
    implicit val conf: UriConfig = UriConfig(encoder = PercentEncoder())
    val uri = Url.parse(
      "http://localhost:9000/mlb/2014/06/15/david-wrights-slump-continues-why-new-york-mets-franchise-third-baseman-must-be-gone-before-seasons-end/?utm_source=RantSports&utm_medium=HUBRecirculation&utm_term=MLBNew York MetsGrid"
    )
    uri.toString should equal(
      "http://localhost:9000/mlb/2014/06/15/david-wrights-slump-continues-why-new-york-mets-franchise-third-baseman-must-be-gone-before-seasons-end/?utm_source=RantSports&utm_medium=HUBRecirculation&utm_term=MLBNew%20York%20MetsGrid"
    )
  }

  "Github Issue #65 example 3" should "now be fixed" in {
    val uri = Url.parse("http://localhost:9000/t?x=y%26")
    uri.query.param("x") should equal(Some("y&"))
    uri.toString should equal("http://localhost:9000/t?x=y%26")
  }

  "Github Issue #65 example 4" should "now be fixed" in {
    val uri = Url.parse("http://localhost/offers.xml?&id=10748337&np=1")
    uri.toString should equal("http://localhost/offers.xml?&id=10748337&np=1")
  }

  "Github Issue #65 example 5" should "now be fixed" in {
    val uri = Url.parse("http://localhost/offers.xml?id=10748337&np=1&")
    uri.toString should equal("http://localhost/offers.xml?id=10748337&np=1&")
  }

  "Github Issue #65 example 6" should "now be fixed" in {
    val uri = Url.parse("http://localhost/offers.xml?id=10748337&np=1&#anchor")
    uri.toString should equal("http://localhost/offers.xml?id=10748337&np=1&#anchor")
  }

  "Github Issue #68" should "now be fixed" in {
    val uri = Url.parse("http://example.com/path?param=something==")
    uri.toString should equal("http://example.com/path?param=something%3D%3D")
  }

  "Github Issue #72" should "now be fixed" in {
    val uri = Url.parse("http://hello.world?email=abc@xyz")
    uri.hostOption should equal(Some(DomainName("hello.world")))
    uri.query.param("email") should equal(Some("abc@xyz"))
  }

  "Github Issue #73" should "now be fixed" in {
    val uri = AbsoluteUrl.parse("http://somewhere.something").withUser("user:1@domain").withPassword("abc xyz")
    uri.toString() should equal("http://user%3A1%40domain:abc%20xyz@somewhere.something")
  }

  "Github Issue #99" should "now be fixed" in {
    val uri = Url.parse("https://www.foo.com/#/myPage?token=bar")
    uri.toString should equal("https://www.foo.com/#/myPage?token=bar")
  }

  "Github Issue #104" should "now be fixed" in {
    val uri = Url.parse("a1+-.://localhost")
    uri.schemeOption should equal(Some("a1+-."))
    uri.hostOption should equal(Some(DomainName("localhost")))
  }

  "Github Issue #106" should "now be fixed" in {
    val p = Url.parse("http://localhost:1234")

    val withPath = p.addPathParts("some", "path", "segments")
    withPath.toString should equal("http://localhost:1234/some/path/segments")

    val withPathAndQuery =
      p.addPathParts("some", "path", "segments").addParam("returnUrl", "http://localhost:1234/some/path/segments")
    withPathAndQuery.toString should equal(
      "http://localhost:1234/some/path/segments?returnUrl=http://localhost:1234/some/path/segments"
    )
  }

  "Github Issue #114" should "now be fixed" in {
    val uri = Url.parse("https://krownlab.com/products/hardware-systems/baldur/#baldur-top-mount#1")
    uri.fragment should equal(Some("baldur-top-mount#1"))
    uri.toString should equal("https://krownlab.com/products/hardware-systems/baldur/#baldur-top-mount%231")
  }
}
