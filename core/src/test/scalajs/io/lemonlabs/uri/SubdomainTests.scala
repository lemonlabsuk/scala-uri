package io.lemonlabs.uri

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class SubdomainTests extends AnyFlatSpec with Matchers {
  "longestSubdomain" should "return a.b.c for http://a.b.c.com/" in {
    val subdomain = Url.parse("http://a.b.c.com/").longestSubdomain
    subdomain should equal(Some("a.b.c"))
  }

  "longestSubdomain" should "return a.b.c for http://a.b.c/" in {
    val subdomain = Url.parse("http://a.b.c/").longestSubdomain
    subdomain should equal(Some("a.b.c"))
  }

  "longestSubdomain" should "return a for apex domain http://a.com/" in {
    val subdomain = Url.parse("http://a.com/").longestSubdomain
    subdomain should equal(Some("a"))
  }

  "longestSubdomain" should "return None for http://.com/" in {
    val subdomain = Url.parse("http://.com/").longestSubdomain
    subdomain should equal(None)
  }

  "subdomain" should "return for http://a.b.c.com/" in {
    val subdomain = Url.parse("http://a.b.c.com/").subdomain
    subdomain should equal(Some("a.b"))
  }

  "subdomain" should "return for http://a.b.c/" in {
    val subdomain = Url.parse("http://a.b.c/").subdomain
    subdomain should equal(Some("a.b"))
  }

  "subdomain" should "return for URL with no public suffix" in {
    val subdomain = Url.parse("http://1.2.3/").subdomain
    subdomain should equal(Some("1.2"))
  }

  "subdomain" should "NOT return for URL with IPv4" in {
    val subdomain = Url.parse("http://1.2.3.4/").subdomain
    subdomain should equal(None)
  }

  "subdomain" should "NOT return for URL with IPv6" in {
    val subdomain = Url.parse("http://[1:2:3:4:5:6:7:8]/").subdomain
    subdomain should equal(None)
  }

  "subdomain" should "return None for apex domain http://a.com/" in {
    val subdomain = Url.parse("http://a.com/").subdomain
    subdomain should equal(None)
  }

  "subdomains" should "return for http://a.b.c.com/" in {
    val subdomains = Url.parse("http://a.b.c.com/").subdomains
    subdomains should equal(Vector("a", "a.b", "a.b.c"))
  }

  "subdomains" should "return for http://a.b.c/" in {
    val subdomains = Url.parse("http://a.b.c/").subdomains
    subdomains should equal(Vector("a", "a.b", "a.b.c"))
  }

  "subdomains" should "return for apex domain http://a.com/" in {
    val subdomains = Url.parse("http://a.com/").subdomains
    subdomains should equal(Vector("a"))
  }

  "shortestSubdomain" should "return a for http://a.b.c.com/" in {
    val subdomain = Url.parse("http://a.b.c.com/").shortestSubdomain
    subdomain should equal(Some("a"))
  }

  "shortestSubdomain" should "return a for http://a.b.c/" in {
    val subdomain = Url.parse("http://a.b.c/").shortestSubdomain
    subdomain should equal(Some("a"))
  }

  "shortestSubdomain" should "return a for apex domain http://a.com/" in {
    val subdomain = Url.parse("http://a.com/").shortestSubdomain
    subdomain should equal(Some("a"))
  }

  "RelativeUrls" should "not return subdomains" in {
    val uri = RelativeUrl.parse("/blah")
    uri.subdomain should equal(None)
    uri.subdomains should equal(Vector.empty)
    uri.shortestSubdomain should equal(None)
    uri.longestSubdomain should equal(None)
  }

  "IPv4s" should "not return subdomains" in {
    val uri = IpV4.parse("1.2.3.4")
    uri.subdomain should equal(None)
    uri.subdomains should equal(Vector.empty)
    uri.shortestSubdomain should equal(None)
    uri.longestSubdomain should equal(None)
  }

  "IPv6s" should "not return subdomains" in {
    val uri = IpV6.parse("[::1]")
    uri.subdomain should equal(None)
    uri.subdomains should equal(Vector.empty)
    uri.shortestSubdomain should equal(None)
    uri.longestSubdomain should equal(None)
  }
}
