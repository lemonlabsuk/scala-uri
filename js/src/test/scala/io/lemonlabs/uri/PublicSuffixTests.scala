package io.lemonlabs.uri

import org.scalatest.{FlatSpec, Matchers}

class PublicSuffixTests extends FlatSpec with Matchers {

  "PublicSuffixes" should "not be supported for ScalaJS" in {
    a[NotImplementedError] should be thrownBy Url.parse("http://www.google.co.uk/blah").publicSuffixes
  }
}
