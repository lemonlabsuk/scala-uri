package com.netaporter.uri

import com.netaporter.uri.config.UriConfig
import org.scalatest.{FlatSpec, Matchers}

class EncodingJvmTests extends FlatSpec with Matchers {

  import dsl._

  "Chinese characters with non-UTF8 encoding" should "be percent encoded" in {
    val uri = "http://theon.github.com/uris-in-scala.html" ? ("chinese" -> "网址")
    val conf = UriConfig(charset = "GB2312")
    uri.toString(conf) should equal ("http://theon.github.com/uris-in-scala.html?chinese=%CD%F8%D6%B7")
  }
}
