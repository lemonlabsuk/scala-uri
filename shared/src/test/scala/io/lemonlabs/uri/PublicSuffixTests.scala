package io.lemonlabs.uri

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class PublicSuffixTests extends AnyFlatSpec with Matchers {
  "Uri publicSuffix method" should "match the longest public suffix" in {
    val uri = Url.parse("http://www.google.co.uk/blah")
    uri.publicSuffix should equal(Some("co.uk"))
  }

  it should "only return public suffixes that match full dot separated host parts" in {
    val uri = Url.parse("http://www.bar.com")

    // Should not match ar.com
    // Github issue #110
    uri.publicSuffix should equal(Some("com"))
  }

  it should "return wildcard public suffixes" in {
    // *.bd from public suffix list Should match com.bd
    // Github issue #179
    Url.parse("http://www.bar.com.bd").publicSuffix should equal(Some("com.bd"))
  }

  it should "not return wildcard exception public suffixes" in {
    // *.ck from public suffix list should not match www.ck because it is an exception
    Url.parse("http://www.bar.www.ck").publicSuffix should equal(None)

    // However other suffixes should match *.ck
    Url.parse("http://www.bar.www1.ck").publicSuffix should equal(Some("www1.ck"))
  }

  "Uri publicSuffixes method" should "match the all public suffixes" in {
    val uri = Url.parse("http://www.google.co.uk/blah")
    uri.publicSuffixes should equal(Vector("co.uk", "uk"))
  }

  it should "return None for relative URLs" in {
    val uri = Url.parse("/blah")
    uri.publicSuffix should equal(None)
  }

  "RelativeUrls" should "not return public suffixes" in {
    val uri = RelativeUrl.parse("/blah")
    uri.publicSuffix should equal(None)
    uri.publicSuffixes should equal(Vector.empty)
  }

  "IPv4s" should "not return public suffixes" in {
    val uri = IpV4.parse("1.2.3.4")
    uri.publicSuffix should equal(None)
    uri.publicSuffixes should equal(Vector.empty)
  }

  "IPv6s" should "not return public suffixes" in {
    val uri = IpV6.parse("[::1]")
    uri.publicSuffix should equal(None)
    uri.publicSuffixes should equal(Vector.empty)
  }
}
