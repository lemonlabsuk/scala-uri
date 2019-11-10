package io.lemonlabs.uri

import org.scalatest.{FlatSpec, Matchers}
import io.lemonlabs.uri.decoding.{NoopDecoder, UriDecodeException}
import io.lemonlabs.uri.config.UriConfig

class DecodingTests extends FlatSpec with Matchers {
  "Reserved characters" should "be percent decoded during parsing" in {
    val uri = Url.parse(
      "http://theon.github.com/uris-in-scala.html?reserved=%3A%2F%3F%23%5B%5D%40%21%24%26%27%28%29%2A%2B%2C%3B%3D%7B%7D%5C%0A%0D"
    )
    uri.toStringRaw should equal("http://theon.github.com/uris-in-scala.html?reserved=:/?#[]@!$&'()*+,;={}\\\n\r")
  }

  it should "be percent decoded in the fragment" in {
    val uri = Url.parse(
      "http://theon.github.com/uris-in-scala.html#%3A%2F%3F%23%5B%5D%40%21%24%26%27%28%29%2A%2B%2C%3B%3D%7B%7D%5C%0A%0D"
    )
    uri.toStringRaw should equal("http://theon.github.com/uris-in-scala.html#:/?#[]@!$&'()*+,;={}\\\n\r")
  }

  "Percent decoding" should "be disabled when requested" in {
    implicit val c = UriConfig(decoder = NoopDecoder)
    val uri = Url.parse(
      "http://theon.github.com/uris-in-scala.html?reserved=%3A%2F%3F%23%5B%5D%40%21%24%26%27%28%29%2A%2B%2C%3B%3D%7B%7D%5C%0A%0D"
    )
    uri.toStringRaw should equal(
      "http://theon.github.com/uris-in-scala.html?reserved=%3A%2F%3F%23%5B%5D%40%21%24%26%27%28%29%2A%2B%2C%3B%3D%7B%7D%5C%0A%0D"
    )
  }

  it should "decode 2-byte groups" in {
    val uri = Url.parse("http://example.com/%C2%A2?cents_sign=%C2%A2")
    uri.toStringRaw should equal("http://example.com/¢?cents_sign=¢")
  }

  it should "decode 3-byte groups" in {
    val uri = Url.parse("http://example.com/%E2%82%AC?euro_sign=%E2%82%AC")
    uri.toStringRaw should equal("http://example.com/€?euro_sign=€")
  }

  it should "decode 4-byte groups" in {
    val uri = Url.parse("http://example.com/%F0%9F%82%A0?ace_of_spades=%F0%9F%82%A1")
    uri.toStringRaw should equal("http://example.com/\uD83C\uDCA0?ace_of_spades=\uD83C\uDCA1")
  }

  "Parsing an non percent encoded URL containing percents" should "throw UriDecodeException" in {
    intercept[UriDecodeException] {
      Url.parse("http://lesswrong.com/index.php?query=abc%yum&john=hello")
    }
    intercept[UriDecodeException] {
      Uri.parse("http://lesswrong.com/index.php?query=abc%%")
    }
    intercept[UriDecodeException] {
      Uri.parse("%")
    }
  }
}
