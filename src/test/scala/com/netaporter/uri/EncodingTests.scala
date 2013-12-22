package com.netaporter.uri

import org.scalatest.{Matchers, FlatSpec}
import com.netaporter.uri.config.UriConfig

class EncodingTests extends FlatSpec with Matchers {

  import dsl._
  import encoding._

  "URI paths" should "be percent encoded" in {
    val uri: Uri = "http://theon.github.com/üris-in-scàla.html"
    uri.toString should equal ("http://theon.github.com/%C3%BCris-in-sc%C3%A0la.html")
  }

  "Raw paths" should "not be encoded" in {
    val uri: Uri = "http://theon.github.com/üris-in-scàla.html"
    uri.pathRaw should equal ("/üris-in-scàla.html")
  }

  "toStringRaw" should "not be encoded" in {
    val uri: Uri = "http://theon.github.com/üris-in-scàla.html" ? ("càsh" -> "£50")
    uri.toStringRaw should equal ("http://theon.github.com/üris-in-scàla.html?càsh=£50")
  }
 
  "URI path spaces" should "be percent encoded by default" in {
    val uri: Uri = "http://theon.github.com/uri with space"
    uri.toString should equal ("http://theon.github.com/uri%20with%20space")
  }

  "URI path spaces" should "be plus encoded if configured" in {
    implicit val config = UriConfig(encoder = percentEncode + spaceAsPlus)
    val uri: Uri = "http://theon.github.com/uri with space"
    uri.toString should equal ("http://theon.github.com/uri+with+space")
  }

  "Path chars" should "be encoded as custom strings if configured" in {
    implicit val config = UriConfig(encoder = percentEncode + encodeCharAs(' ', "_"))
    val uri: Uri = "http://theon.github.com/uri with space"
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
  
  "Chinese characters with non-UTF8 encoding" should "be percent encoded" in {
    val uri = "http://theon.github.com/uris-in-scala.html" ? ("chinese" -> "网址")
    val conf = UriConfig(charset = "GB2312")
    uri.toString(conf) should equal ("http://theon.github.com/uris-in-scala.html?chinese=%CD%F8%D6%B7")
  }

  "Russian characters" should "be percent encoded" in {
    val uri = "http://theon.github.com/uris-in-scala.html" ? ("russian" -> "Скала")
    uri.toString should equal ("http://theon.github.com/uris-in-scala.html?russian=%D0%A1%D0%BA%D0%B0%D0%BB%D0%B0")
  }

  "Fragments" should "be percent encoded" in {
    val uri = "http://theon.github.com/uris-in-scala.html" ? ("chinese" -> "网址")
    uri.toString should equal ("http://theon.github.com/uris-in-scala.html?chinese=%E7%BD%91%E5%9D%80")
  }

  "Percent encoding with custom reserved characters" should "be easy" in {
    implicit val config = UriConfig(encoder = percentEncode('#'))
    val uri = "http://theon.github.com/uris-in-scala.html" ? ("reserved" -> ":/?#[]@!$&'()*+,;={}\\\n\r")
    uri.toString should equal ("http://theon.github.com/uris-in-scala.html?reserved=:/?%23[]@!$&'()*+,;={}\\\n\r")
  }

  "Percent encoding with a few less reserved characters that the defaults" should "be easy" in {
    implicit val config = UriConfig(encoder = percentEncode -- '+')
    val uri = "http://theon.github.com/uris-in-scala.html" ? ("reserved" -> ":/?#[]@!$&'()*+,;={}\\\n\r")
    uri.toString should equal ("http://theon.github.com/uris-in-scala.html?reserved=%3A%2F%3F%23%5B%5D%40%21%24%26%27%28%29%2A+%2C%3B%3D%7B%7D%5C%0A%0D")
  }

  "Percent encoding with a few extra reserved characters on top of the defaults" should "be easy" in {
    implicit val config = UriConfig(encoder = percentEncode() ++ ('a', 'b'))
    val uri: Uri = "http://theon.github.com/abcde"
    uri.toString should equal ("http://theon.github.com/%61%62cde")
  }
}
