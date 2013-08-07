package com.github.theon.uri

import org.scalatest.FlatSpec
import org.scalatest.matchers.ShouldMatchers
import com.github.theon.uri.Uri._

/**
 * Date: 29/06/2013
 * Time: 17:41
 */
class DecodingTests extends FlatSpec with ShouldMatchers {

  "Reserved characters" should "be percent decoded during parsing" in {
    val uri: Uri = "http://theon.github.com/uris-in-scala.html?reserved=%3A%2F%3F%23%5B%5D%40%21%24%26%27%28%29%2A%2B%2C%3B%3D%7B%7D%5C%0A%0D"
    uri.toStringRaw should equal ("http://theon.github.com/uris-in-scala.html?reserved=:/?#[]@!$&'()*+,;={}\\\n\r")
  }

  "Percent decoding" should "be disabled when requested" in {
    implicit val decoder = NoopDecoder
    val uri = Uri.parseUri("http://theon.github.com/uris-in-scala.html?reserved=%3A%2F%3F%23%5B%5D%40%21%24%26%27%28%29%2A%2B%2C%3B%3D%7B%7D%5C%0A%0D")
    uri.toStringRaw should equal ("http://theon.github.com/uris-in-scala.html?reserved=%3A%2F%3F%23%5B%5D%40%21%24%26%27%28%29%2A%2B%2C%3B%3D%7B%7D%5C%0A%0D")
  }

  "Parsing an non percent encoded URL containing percents" should "throw UriDecodeException" in {
    intercept[UriDecodeException] {
      parseUri("http://lesswrong.com/index.php?query=abc%yum&john=hello")
    }
  }
}
