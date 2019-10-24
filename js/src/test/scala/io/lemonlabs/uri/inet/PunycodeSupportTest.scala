package io.lemonlabs.uri.inet

import org.scalatest.{FlatSpec, Matchers}

class PunycodeSupportTest extends FlatSpec with Matchers {
  "Punycode" should "throw a non-supported exception for Scala.js" in {
    a[NotImplementedError] should be thrownBy new PunycodeSupport {}.toPunycode("abc")
  }
}
