package com.netaporter.uri

import org.scalatest.FlatSpec
import org.scalatest.Matchers

class PublicSuffixTests extends FlatSpec with Matchers {

  "Uri publicSuffix method" should "match the longest public suffix" in {
    val uri = Uri.parse("http://www.google.co.uk/blah")
    uri.publicSuffix should equal(Some("co.uk"))
  }

  it should "only return public suffixes that match full dot separated host parts" in {
    val uri = Uri.parse("http://www.bar.com")

    // Should not match ar.com
    // Github issue #110
    uri.publicSuffix should equal(Some("com"))
  }

  "Uri publicSuffixes method" should "match the all public suffixes" in {
    val uri = Uri.parse("http://www.google.co.uk/blah")
    uri.publicSuffixes should equal(Seq("co.uk", "uk"))
  }

  it should "return None for relative URLs" in {
    val uri = Uri.parse("/blah")
    uri.publicSuffix should equal(None)
  }
}
