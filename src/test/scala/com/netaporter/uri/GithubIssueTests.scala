package com.netaporter.uri

import org.scalatest.{Matchers, OptionValues, FlatSpec}
import scala.Some
import Uri._
import com.netaporter.uri.decoding.PermissivePercentDecoder
import com.netaporter.uri.config.UriConfig
import com.netaporter.uri

/**
 * Test Suite to ensure that bugs raised by awesome github peeps NEVER come back
 */
class GithubIssueTests extends FlatSpec with Matchers with OptionValues {

  import uri.dsl._

  "Github Issue #2" should "now be fixed. Pluses in querystrings should be encoded" in {
    val uri = "http://theon.github.com/+" ? ("+" -> "+")
    uri.toString should equal ("http://theon.github.com/%2B?%2B=%2B")
  }

  "Github Issue #4" should "now be fixed. Port numbers should be rendered by toString" in {
    val uri = "http://theon.github.com:8080/test" ? ("p" -> "1")
    uri.toString should equal ("http://theon.github.com:8080/test?p=1")
  }

  "Github Issue #5" should "now be fixed. The characters {} should now be percent encoded" in {
    val uri = "http://theon.github.com:8080/{}" ? ("{}" -> "{}")
    uri.toString should equal ("http://theon.github.com:8080/%7B%7D?%7B%7D=%7B%7D")
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
    uri.query.params("oi") should equal (Vector("TscV16GUGtlU"))
    uri.query.params("ppc") should equal (Vector(""))
    uri.query.params("bpc") should equal (Vector(""))
  }

  "Github Issue #12" should "now be fixed. Parsing URIs parse percent escapes" in {
    val source = new Uri(
      Some("http"),
      None,
      None,
      Some("xn--ls8h.example.net"),
      None,
      List(PathPart(""), PathPart("path with spaces")),
      QueryString(Vector("a b" -> "c d")),
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
    u.query.param("query").value should equal("abc%yum")
  }

  "Github Issue #37" should "now be fixed" in {
    val uri = "http://test.com:8080" / "something"
    uri.toString should equal("http://test.com:8080/something")
  }
}
