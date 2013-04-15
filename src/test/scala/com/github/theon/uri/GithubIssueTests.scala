package com.github.theon.uri

import org.scalatest.FlatSpec
import org.scalatest.matchers.ShouldMatchers
import com.github.theon.uri.Uri._

/**
 * Test Suite to ensure that bugs raised by awesome github peeps NEVER come back
 */
class GithubIssueTests  extends FlatSpec with ShouldMatchers {

  "Github Issue #2" should " now be fixed. Pluses in querystrings should be encoded" in {
    val uri = "http://theon.github.com/+" ? ("+" -> "+")
    uri.toString should equal ("http://theon.github.com/%2B?%2B=%2B")
  }

  "Github Issue #4" should " now be fixed. Port numbers should be rendered by toString" in {
    val uri = "http://theon.github.com:8080/test" ? ("p" -> "1")
    uri.toString should equal ("http://theon.github.com:8080/test?p=1")
  }

  "Github Issue #5" should " now be fixed. The characters {} should now be percent encoded" in {
    val uri = "http://theon.github.com:8080/{}" ? ("{}" -> "{}")
    uri.toString should equal ("http://theon.github.com:8080/%7B%7D?%7B%7D=%7B%7D")
  }

  "Github Issue #6" should " now be fixed. No implicit Encoder val required for implicit Uri -> String conversion " in {
    val uri = "/blah" ? ("blah" -> "blah")
    val uriString:String = uri
    uriString should equal ("/blah?blah=blah")
  }

  "Github Issue #7" should " now be fixed. Calling uri.toString() (with parentheses) should now behave the same as uri.toString " in {
    val uri = "/blah" ? ("blah" -> "blah")
    uri.toString() should equal ("/blah?blah=blah")
  }

  "Github Issue #8" should " now be fixed. Parsed relative uris should have no protocol" in {
    val uri = parseUri("abc")

    uri.protocol should equal (None)
    uri.host should equal (None)
    uri.path should equal ("/abc")
  }
}
