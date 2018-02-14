package com.netaporter.uri

import com.netaporter.uri.config.UriConfig
import org.scalatest.{FlatSpec, Matchers}

class EncodingJvmTests extends FlatSpec with Matchers {

  "Chinese characters with non-UTF8 encoding" should "be percent encoded" in {
    implicit val conf = UriConfig(charset = "GB2312")
    val url = Url.parse("http://theon.github.com/uris-in-scala.html?chinese=网址")
    url.toString should equal ("http://theon.github.com/uris-in-scala.html?chinese=%CD%F8%D6%B7")
  }
}
