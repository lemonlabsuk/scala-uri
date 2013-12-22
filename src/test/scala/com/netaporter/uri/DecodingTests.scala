package com.netaporter.uri

import org.scalatest.{Matchers, FlatSpec}
import com.netaporter.uri.decoding.{UriDecodeException, NoopDecoder}
import com.netaporter.uri.config.UriConfig

/**
 * Date: 29/06/2013
 * Time: 17:41
 */
class DecodingTests extends FlatSpec with Matchers {

  "Reserved characters" should "be percent decoded during parsing" in {
    val uri = Uri.parse("http://theon.github.com/uris-in-scala.html?reserved=%3A%2F%3F%23%5B%5D%40%21%24%26%27%28%29%2A%2B%2C%3B%3D%7B%7D%5C%0A%0D")
    uri.toStringRaw() should equal ("http://theon.github.com/uris-in-scala.html?reserved=:/?#[]@!$&'()*+,;={}\\\n\r")
  }

  "Percent decoding" should "be disabled when requested" in {
    implicit val c = UriConfig(decoder = NoopDecoder)
    val uri = Uri.parse("http://theon.github.com/uris-in-scala.html?reserved=%3A%2F%3F%23%5B%5D%40%21%24%26%27%28%29%2A%2B%2C%3B%3D%7B%7D%5C%0A%0D")
    uri.toStringRaw() should equal ("http://theon.github.com/uris-in-scala.html?reserved=%3A%2F%3F%23%5B%5D%40%21%24%26%27%28%29%2A%2B%2C%3B%3D%7B%7D%5C%0A%0D")
  }

  "Parsing an non percent encoded URL containing percents" should "throw UriDecodeException" in {
    intercept[UriDecodeException] {
      Uri.parse("http://lesswrong.com/index.php?query=abc%yum&john=hello")
    }
  }
}
