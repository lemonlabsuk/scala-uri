package com.github.theon.urlutils

import org.scalatest._
import org.scalatest.matchers._
import com.github.theon.uri.Uri
import com.github.theon.uri.Uri._

class DslTests extends FlatSpec with ShouldMatchers {

  "A simple absolute URI" should "render correctly" in {
    val uri:Uri = "http://theon.github.com/uris-in-scala.html"
    uri.toString should equal ("http://theon.github.com/uris-in-scala.html")
  }

  "A simple relative URI" should "render correctly" in {
    val uri:Uri = "/uris-in-scala.html"
    uri.toString should equal ("/uris-in-scala.html")
  }

  "An absolute URI with querystring params" should "render correctly" in {
    val uri = "http://theon.github.com/uris-in-scala.html" ? ("testOne" -> "1") & ("testTwo" -> "2")
    uri.toString should equal ("http://theon.github.com/uris-in-scala.html?testOne=1&testTwo=2")
  }

  "A relative URI with querystring params" should "render correctly" in {
    val uri = "/uris-in-scala.html" ? ("testOne" -> "1") & ("testTwo" -> "2")
    uri.toString should equal ("/uris-in-scala.html?testOne=1&testTwo=2")
  }

  "Multiple querystring params with the same key" should "render correctly" in {
    val uri = "/uris-in-scala.html" ? ("testOne" -> "1") & ("testOne" -> "2")
    uri.toString should equal ("/uris-in-scala.html?testOne=2&testOne=1")
  }
}
