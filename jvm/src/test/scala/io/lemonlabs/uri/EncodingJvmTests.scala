package io.lemonlabs.uri

import io.lemonlabs.uri.config.UriConfig
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class EncodingJvmTests extends AnyFlatSpec with Matchers {
  "Chinese characters with non-UTF8 encoding" should "be percent encoded" in {
    implicit val conf = UriConfig(charset = "GB2312")
    val url = Url.parse("http://theon.github.com/uris-in-scala.html?chinese=网址")
    url.toString should equal("http://theon.github.com/uris-in-scala.html?chinese=%CD%F8%D6%B7")
  }
}
