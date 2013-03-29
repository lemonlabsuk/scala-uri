package com.github.theon.urlutils

import org.scalatest.FlatSpec
import org.scalatest.matchers.ShouldMatchers
import com.github.theon.uri.{EncodeCharAs, Uri}
import com.github.theon.uri.Uri._
import com.github.theon.uri.Encoders._

class EncodingTests extends FlatSpec with ShouldMatchers {

  "URI paths" should "be percent encoded" in {
    val uri:Uri = "http://theon.github.com/üris-in-scàla.html"
    uri.toString should equal ("http://theon.github.com/%C3%BCris-in-sc%C3%A0la.html")
  }

  "Raw paths" should "not be encoded" in {
    val uri:Uri = "http://theon.github.com/üris-in-scàla.html" ? ("càsh" -> "£50")
    uri.pathRaw should equal ("/üris-in-scàla.html?càsh=£50")
  }

  "toStringRaw" should "not be encoded" in {
    val uri:Uri = "http://theon.github.com/üris-in-scàla.html" ? ("càsh" -> "£50")
    uri.toStringRaw should equal ("http://theon.github.com/üris-in-scàla.html?càsh=£50")
  }

  "URI path spaces" should "be percent encoded by default" in {
    val uri:Uri = "http://theon.github.com/uri with space"
    uri.toString should equal ("http://theon.github.com/uri%20with%20space")
  }

  "URI path spaces" should "be plus encoded if configured" in {
    implicit val encoder = PercentEncoder + EncodeSpaceAsPlus
    val uri:Uri = "http://theon.github.com/uri with space"
    uri.toString should equal ("http://theon.github.com/uri+with+space")
  }

  "Path chars" should "be encoded as custom strings if configured" in {
    implicit val encoder = PercentEncoder + EncodeCharAs(' ', "_")
    val uri:Uri = "http://theon.github.com/uri with space"
    uri.toString should equal ("http://theon.github.com/uri_with_space")
  }

  "Querystring parameters" should "be percent encoded" in {
    val uri = "http://theon.github.com/uris-in-scala.html" ? ("càsh" -> "£50") & ("©opyright" -> "false")
    uri.toString should equal ("http://theon.github.com/uris-in-scala.html?c%C3%A0sh=%C2%A350&%C2%A9opyright=false")
  }

  "Reserved characters" should "be percent encoded" in {
    val uri = "http://theon.github.com/uris-in-scala.html" ? ("reserved" -> ":/?#[]@!$&'()*+,;={}\\\n\r")
    uri.toString should equal ("http://theon.github.com/uris-in-scala.html?reserved=%3A%2F%3F%23%5B%5D%40%21%24%26%27%28%29%2A%2B%2C%3B%3D%7B%7D%5C%0A%0D")
  }

  "Chinese characters" should "be percent encoded" in {
    val uri = "http://theon.github.com/uris-in-scala.html" ? ("chinese" -> "网址")
    uri.toString should equal ("http://theon.github.com/uris-in-scala.html?chinese=%E7%BD%91%E5%9D%80")
  }

  "Russian characters" should "be percent encoded" in {
    val uri = "http://theon.github.com/uris-in-scala.html" ? ("russian" -> "Скала")
    uri.toString should equal ("http://theon.github.com/uris-in-scala.html?russian=%D0%A1%D0%BA%D0%B0%D0%BB%D0%B0")
  }
}
