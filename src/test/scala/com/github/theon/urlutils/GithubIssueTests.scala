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
}
