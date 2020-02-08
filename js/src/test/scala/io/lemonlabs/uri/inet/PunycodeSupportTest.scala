package io.lemonlabs.uri.inet

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class PunycodeSupportTest extends AnyFlatSpec with Matchers {
  "Punycode" should "throw a non-supported exception for Scala.js" in {
    a[NotImplementedError] should be thrownBy new PunycodeSupport {}.toPunycode("abc")
  }
}
