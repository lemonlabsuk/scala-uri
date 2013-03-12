package com.github.theon.urlutils

import org.scalatest.FlatSpec
import org.scalatest.matchers.ShouldMatchers
import com.github.theon.uri.Uri

/**
 * Date: 12/03/2013
 * Time: 17:33
 */
class ProtocolTests extends FlatSpec with ShouldMatchers {

  "A domain with no protocol" should "be rendered as a protocol relative url" in {
    val uri = Uri(None, "theon.github.com", "/uris-in-scala.html")
    uri.toString should equal ("//theon.github.com/uris-in-scala.html")
  }

  "A domain with a protocol" should "be rendered as a protocol absolute url" in {
    val uri = Uri(Some("ftp"), "theon.github.com", "/uris-in-scala.html")
    uri.toString should equal ("ftp://theon.github.com/uris-in-scala.html")
  }
}
