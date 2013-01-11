package com.github.theon.urlutils

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
}
