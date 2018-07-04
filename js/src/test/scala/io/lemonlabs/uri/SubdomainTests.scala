package io.lemonlabs.uri

import org.scalatest.{FlatSpec, Matchers}

/**
  * PublicSuffixes and Subdomains are not implemented yet for scala-js
  */
class SubdomainTests extends FlatSpec with Matchers {

  "longestSubdomain" should "return a.b.c for http://a.b.c.com/" in {
    a[NotImplementedError] should be thrownBy Url.parse("http://a.b.c.com/").longestSubdomain
  }

  "longestSubdomain" should "return a.b.c for http://a.b.c/" in {
    a[NotImplementedError] should be thrownBy Url.parse("http://a.b.c/").longestSubdomain
  }

  "longestSubdomain" should "return a for apex domain http://a.com/" in {
    a[NotImplementedError] should be thrownBy Url.parse("http://a.com/").longestSubdomain
  }

  "longestSubdomain" should "return None for http://.com/" in {
    a[NotImplementedError] should be thrownBy Url.parse("http://.com/").longestSubdomain
  }

  "subdomain" should "return for http://a.b.c.com/" in {
    a[NotImplementedError] should be thrownBy Url.parse("http://a.b.c.com/").subdomain
  }

  "subdomain" should "return for http://a.b.c/" in {
    a[NotImplementedError] should be thrownBy Url.parse("http://a.b.c/").subdomain
  }

  "subdomain" should "return None for apex domain http://a.com/" in {
    a[NotImplementedError] should be thrownBy Url.parse("http://a.com/").subdomain
  }

  "subdomains" should "return for http://a.b.c.com/" in {
    a[NotImplementedError] should be thrownBy Url.parse("http://a.b.c.com/").subdomains
  }

  "subdomains" should "return for http://a.b.c/" in {
    a[NotImplementedError] should be thrownBy Url.parse("http://a.b.c/").subdomains
  }

  "subdomains" should "return for apex domain http://a.com/" in {
    a[NotImplementedError] should be thrownBy Url.parse("http://a.com/").subdomains
  }

  "shortestSubdomain" should "return a for http://a.b.c.com/" in {
    a[NotImplementedError] should be thrownBy Url.parse("http://a.b.c.com/").shortestSubdomain
  }

  "shortestSubdomain" should "return a for http://a.b.c/" in {
    a[NotImplementedError] should be thrownBy Url.parse("http://a.b.c/").shortestSubdomain
  }

  "shortestSubdomain" should "return a for apex domain http://a.com/" in {
    a[NotImplementedError] should be thrownBy Url.parse("http://a.com/").shortestSubdomain
  }
}
