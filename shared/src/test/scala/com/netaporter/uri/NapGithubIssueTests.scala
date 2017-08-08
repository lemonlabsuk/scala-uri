package com.netaporter.uri

import org.scalatest.{Matchers, OptionValues, FlatSpec}
import scala.Some
import Uri._
import com.netaporter.uri.decoding.PermissivePercentDecoder
import com.netaporter.uri.config.UriConfig
import com.netaporter.uri
import org.parboiled2.ParseError

/**
 * Test Suite to ensure that bugs raised by awesome github peeps NEVER come back
  *
  * These bugs were raised at the old github page https://github.com/net-a-porter/scala-uri
 */
class NapGithubIssueTests extends FlatSpec with Matchers with OptionValues {

  import uri.dsl._

  "Github Issue #2" should "now be fixed. Pluses in querystrings should be encoded when using the conservative encoder" in {
    val uri = "http://theon.github.com/" ? ("+" -> "+")
    uri.toString(UriConfig.conservative) should equal ("http://theon.github.com/?%2B=%2B")
  }

  "Github Issue #4" should "now be fixed. Port numbers should be rendered by toString" in {
    val uri = "http://theon.github.com:8080/test" ? ("p" -> "1")
    uri.toString should equal ("http://theon.github.com:8080/test?p=1")
  }

  "Github Issue #5" should "now be fixed. The characters {} should now be percent encoded" in {
    val uri = ("http://theon.github.com" / "{}") ? ("{}" -> "{}")
    uri.toString should equal("http://theon.github.com/%7B%7D?%7B%7D=%7B%7D")
  }

  "Github Issue #6" should "now be fixed. No implicit Encoder val required for implicit Uri -> String conversion " in {
    val uri = "/blah" ? ("blah" -> "blah")
    val uriString: String = uri
    uriString should equal ("/blah?blah=blah")
  }

  "Github Issue #7" should "now be fixed. Calling uri.toString() (with parentheses) should now behave the same as uri.toString " in {
    val uri = "/blah" ? ("blah" -> "blah")
    uri.toString() should equal ("/blah?blah=blah")
  }

  "Github Issue #8" should "now be fixed. Parsed relative uris should have no scheme" in {
    val uri = parse("abc")

    uri.scheme should equal (None)
    uri.host should equal (None)
    uri.path should equal ("/abc")
  }

  "Github Issue #15" should "now be fixed. Empty Query String values are parsed" in {
    val uri = parse("http://localhost:8080/ping?oi=TscV16GUGtlU&ppc=&bpc=")

    uri.scheme.value should equal ("http")
    uri.host.value should equal ("localhost")
    uri.port.value should equal (8080)
    uri.path should equal ("/ping")
    uri.query.params("oi") should equal (Vector(Some("TscV16GUGtlU")))
    uri.query.params("ppc") should equal (Vector(Some("")))
    uri.query.params("bpc") should equal (Vector(Some("")))
  }

  "Github Issue #12" should "now be fixed. Parsing URIs parse percent escapes" in {
    val source = new Uri(
      Some("http"),
      None,
      None,
      Some("xn--ls8h.example.net"),
      None,
      List(PathPart(""), PathPart("path with spaces")),
      QueryString(Vector("a b" -> Some("c d"))),
      None
    )
    val parsed = parse(source.toString)
    parsed should equal(source)
  }

  "Github Issue #19" should "now be fixed" in {
    val uri: Uri = "/coldplay.com?singer=chris%26will"
    uri.toString should equal ("/coldplay.com?singer=chris%26will")
  }

  "Github Issue #26" should "now be fixed" in {
    val uri = "http://lesswrong.com/index.php?query=abc%yum&john=hello"
    val conf = UriConfig(decoder = PermissivePercentDecoder)
    val u = parse(uri)(conf)
    u.query.param("query") should equal(Some("abc%yum"))
  }

  "Github Issue #37" should "now be fixed" in {
    val uri = "http://test.com:8080" / "something"
    uri.toString should equal("http://test.com:8080/something")
  }

  "Github Issue #55" should "now be fixed" in {
    val uri: Uri = "http://localhost:9002/iefjiefjief-efefeffe-fefefee/toto?access_token=ijifjijef-fekieifj-fefoejfoef&gquery=filter(time_before_closing%3C=45)"
    uri.query.param("gquery") should equal(Some("filter(time_before_closing<=45)"))
    uri.toString should equal("http://localhost:9002/iefjiefjief-efefeffe-fefefee/toto?access_token=ijifjijef-fekieifj-fefoejfoef&gquery=filter(time_before_closing%3C%3D45)")
  }

  "Github Issue #56" should "now be fixed" in {
    val ex = the [java.net.URISyntaxException] thrownBy Uri.parse("http://test.net:8o8o")
    ex.getMessage should startWith("Invalid URI could not be parsed.")
  }

  "Github Issue #65 example 1" should "now be fixed" in {
    val uri = Uri.parse("http://localhost:9000/?foo=test&&bar=test")
    uri.toString should equal("http://localhost:9000/?foo=test&&bar=test")
  }

  "Github Issue #65 example 2" should "now be fixed" in {
    val uri = Uri.parse("http://localhost:9000/mlb/2014/06/15/david-wrights-slump-continues-why-new-york-mets-franchise-third-baseman-must-be-gone-before-seasons-end/?utm_source=RantSports&utm_medium=HUBRecirculation&utm_term=MLBNew York MetsGrid")
    uri.toString should equal("http://localhost:9000/mlb/2014/06/15/david-wrights-slump-continues-why-new-york-mets-franchise-third-baseman-must-be-gone-before-seasons-end/?utm_source=RantSports&utm_medium=HUBRecirculation&utm_term=MLBNew%20York%20MetsGrid")
  }

  "Github Issue #65 example 3" should "now be fixed" in {
    val uri = Uri.parse("http://localhost:9000/t?x=y%26")
    uri.query.param("x") should equal(Some("y&"))
    uri.toString should equal("http://localhost:9000/t?x=y%26")
  }

  "Github Issue #65 example 4" should "now be fixed" in {
    val uri = Uri.parse("http://localhost/offers.xml?&id=10748337&np=1")
    uri.toString should equal("http://localhost/offers.xml?&id=10748337&np=1")
  }

  "Github Issue #65 example 5" should "now be fixed" in {
    val uri = Uri.parse("http://localhost/offers.xml?id=10748337&np=1&")
    uri.toString should equal("http://localhost/offers.xml?id=10748337&np=1&")
  }

  "Github Issue #65 example 6" should "now be fixed" in {
    val uri = Uri.parse("http://localhost/offers.xml?id=10748337&np=1&#anchor")
    uri.toString should equal("http://localhost/offers.xml?id=10748337&np=1&#anchor")
  }

  "Github Issue #68" should "now be fixed" in {
    val uri = ("http://example.com/path" ? ("param" -> "something==")).toString
    uri.toString should equal("http://example.com/path?param=something%3D%3D")
  }

  "Github Issue #72" should "now be fixed" in {
    val uri = Uri.parse("http://hello.world?email=abc@xyz")
    uri.host should equal(Some("hello.world"))
    uri.query.param("email") should equal(Some("abc@xyz"))
  }

  "Github Issue #73" should "now be fixed" in {
    val uri = "http://somewhere.something".withUser("user:1@domain").withPassword("abc xyz")
    uri.toString should equal("http://user%3A1%40domain:abc%20xyz@somewhere.something")
  }

  "Github Issue #99" should "now be fixed" in {
    val uri = Uri.parse("https://www.foo.com/#/myPage?token=bar")
    uri.toString should equal("https://www.foo.com/#/myPage?token=bar")
  }

  "Github Issue #104" should "now be fixed" in {
    val uri = Uri.parse("a1+-.://localhost")
    uri.scheme should equal(Some("a1+-."))
    uri.host should equal(Some("localhost"))
  }

  "Github Issue #106" should "now be fixed" in {
    val p = "http://localhost:1234"

    val withPath = p / "some/path/segments"
    withPath.toString should equal("http://localhost:1234/some/path/segments")

    val withPathAndQuery = p / "some/path/segments" ? ("returnUrl" -> "http://localhost:1234/some/path/segments")
    withPathAndQuery.toString should equal("http://localhost:1234/some/path/segments?returnUrl=http://localhost:1234/some/path/segments")
  }

  "Github Issue #114" should "now be fixed" in {
    val uri = Uri.parse("https://krownlab.com/products/hardware-systems/baldur/#baldur-top-mount#1")
    uri.fragment should equal(Some("baldur-top-mount#1"))
    uri.toString should equal("https://krownlab.com/products/hardware-systems/baldur/#baldur-top-mount%231")
  }

  "Github Issue #124" should "now be fixed" in {
    val uri = Uri.parse("https://github.com")
    uri.matrixParams should equal(Seq.empty)
  }
}
