package com.netaporter.uri

import org.scalatest.{Matchers, FlatSpec}

/**
 * Date: 12/03/2013
 * Time: 17:33
 */
class ProtocolTests extends FlatSpec with Matchers {

  import dsl._

  "A domain with no scheme" should "be rendered as a scheme relative url" in {
    val uri = Uri(host = "theon.github.com") / "uris-in-scala.html"
    uri.toString should equal ("//theon.github.com/uris-in-scala.html")
  }

  "A domain with a scheme" should "be rendered as a scheme absolute url" in {
    val uri = Uri(scheme = "ftp", host = "theon.github.com") / "uris-in-scala.html"
    uri.toString should equal ("ftp://theon.github.com/uris-in-scala.html")
  }
}
