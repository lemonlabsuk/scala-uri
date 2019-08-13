package io.lemonlabs.uri

import org.scalatest.{FlatSpec, Matchers}

class DataUrlTests extends FlatSpec with Matchers {

  "Missing mediatype" should "default to text/plain;charset=US-ASCII" in {
    val dataUrl = DataUrl.parse("data:,A%20brief%20note")
    dataUrl.mediaType.toString should equal("")
    dataUrl.mediaType.rawValue should equal(None)
    dataUrl.mediaType.value should equal("text/plain")
    dataUrl.mediaType.rawCharset should equal(None)
    dataUrl.mediaType.charset should equal("US-ASCII")
  }
}
