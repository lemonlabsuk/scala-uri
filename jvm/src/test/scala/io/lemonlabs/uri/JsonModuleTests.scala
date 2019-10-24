package io.lemonlabs.uri

import io.lemonlabs.uri.json.{CirceSupport, JsonSupport, SprayJsonSupport}
import org.scalatest.{FlatSpec, Matchers}

class JsonModuleTests extends FlatSpec with Matchers {
  "SprayJsonSupport" should "return public suffixes" in {
    implicit val jsonSupport: JsonSupport = SprayJsonSupport
    val uri = Url.parse("http://www.google.co.uk/blah")
    uri.publicSuffixes should equal(Vector("co.uk", "uk"))
  }

  "CirceSupport" should "return public suffixes" in {
    implicit val jsonSupport: JsonSupport = CirceSupport
    val uri = Url.parse("http://www.google.co.uk/blah")
    uri.publicSuffixes should equal(Vector("co.uk", "uk"))
  }
}
