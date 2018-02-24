package com.netaporter.uri

import org.scalatest.FlatSpec
import org.scalatest.Matchers

class ApplyTests extends FlatSpec with Matchers {

  "Url apply method" should "accept scheme, host and path" in {
    val url = Url(scheme = "http", host = "theon.github.com", path = "/blah")
    url shouldBe an [AbsoluteUrl]
    url.schemeOption should equal(Some("http"))
    url.hostOption should equal(Some(DomainName("theon.github.com")))
    url.path.toString() should equal("/blah")
    url.query should equal(QueryString.empty)
  }

  it should "accept scheme and host" in {
    val url = Url(scheme = "http", host = "example.com")
    url shouldBe an [AbsoluteUrl]
    url.schemeOption should equal(Some("http"))
    url.hostOption should equal(Some(DomainName("example.com")))
    url.path.toString() should equal("")
    url.query should equal(QueryString.empty)
  }

  it should "accept host and path" in {
    val url = Url(host = "example.com", path = "/example")
    url shouldBe an [ProtocolRelativeUrl]
    url.schemeOption should equal(None)
    url.hostOption should equal(Some(DomainName("example.com")))
    url.path.toString() should equal("/example")
    url.query should equal(QueryString.empty)
  }

  it should "accept scheme and path" in {
    val url = Url(scheme = "mailto", path = "example@example.com")
    url shouldBe an [UrlWithoutAuthority]
    url.schemeOption should equal(Some("mailto"))
    url.hostOption should equal(None)
    url.path.toString() should equal("example@example.com")
    url.query should equal(QueryString.empty)
  }

  it should "accept QueryString" in {
    val qs = QueryString.fromPairs("testKey" -> "testVal")
    val url = Url(query = qs)
    url shouldBe an [RelativeUrl]
    url.schemeOption should equal(None)
    url.hostOption should equal(None)
    url.query should equal(qs)
  }

  it should "accept scheme, host and QueryString" in {
    val qs = QueryString.fromPairs("testKey" -> "testVal")
    val url = Url(scheme = "http", host = "theon.github.com", query = qs)
    url shouldBe an [AbsoluteUrl]
    url.schemeOption should equal(Some("http"))
    url.hostOption should equal(Some(DomainName("theon.github.com")))
    url.query should equal(qs)
  }

  it should "accept NOT allow host with path staring with non-slash" in {
    val thrown = the [IllegalArgumentException] thrownBy
      Url(host = "example.com", path = "example")

    thrown.getMessage should equal("requirement failed: A Url with an authority must either have an empty path or a " +
                                   "path that begins with a slash ('/') character. RFC 3986 section 3.2.")
  }
}
