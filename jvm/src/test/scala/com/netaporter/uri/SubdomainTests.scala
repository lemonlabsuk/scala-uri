package com.netaporter.uri

import org.scalatest.{FlatSpec, Matchers}

class SubdomainTests extends FlatSpec with Matchers {

  "longestSubdomain" should "return a.b.c for http://a.b.c.com/" in {
    val subdomain = Url.parse("http://a.b.c.com/").longestSubdomain
    subdomain should equal(Some("a.b.c"))
  }

  "longestSubdomain" should "return a.b.c for http://a.b.c/" in {
    val subdomain = Url.parse("http://a.b.c/").longestSubdomain
    subdomain should equal(Some("a.b.c"))
  }

  "longestSubdomain" should "return a for root domain http://a.com/" in {
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

  "subdomain" should "return None for root domain http://a.com/" in {
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

  "subdomains" should "return for root domain http://a.com/" in {
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

  "shortestSubdomain" should "return a for root domain http://a.com/" in {
    val subdomain = Url.parse("http://a.com/").shortestSubdomain
    subdomain should equal(Some("a"))
  }
}
