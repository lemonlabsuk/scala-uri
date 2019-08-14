package io.lemonlabs.uri

import org.scalatest.{FlatSpec, Matchers}

class ProtocolTests extends FlatSpec with Matchers {

  "A domain with no scheme" should "be rendered as a scheme relative url" in {
    val url = Url(host = "theon.github.com", path = "/uris-in-scala.html")
    url.toString should equal("//theon.github.com/uris-in-scala.html")
  }

  "A domain with a scheme" should "be rendered as a scheme absolute url" in {
    val url = Url(scheme = "ftp", host = "theon.github.com", path = "/uris-in-scala.html")
    url.toString should equal("ftp://theon.github.com/uris-in-scala.html")
  }
}
