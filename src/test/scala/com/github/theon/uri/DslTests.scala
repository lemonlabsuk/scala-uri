package com.github.theon.uri

import org.scalatest.FlatSpec
import org.scalatest.matchers.ShouldMatchers
import com.github.theon.uri.Uri._

class DslTests extends FlatSpec with ShouldMatchers {

  "A simple absolute URI" should "render correctly" in {
    val uri: Uri = "http://theon.github.com/uris-in-scala.html"
    uri.toString should equal ("http://theon.github.com/uris-in-scala.html")
  }

  "A simple relative URI" should "render correctly" in {
    val uri: Uri = "/uris-in-scala.html"
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

  "Legacy replace method" should "stil replace parameters for the meantime" in {
    val uri = "/uris-in-scala.html" ? ("testOne" -> "1")
    val newUri = uri.replace("testOne", "2")
    newUri.toString should equal ("/uris-in-scala.html?testOne=2")
  }

  "Replace param method" should "replace single parameters with a String argument" in {
    val uri = "/uris-in-scala.html" ? ("testOne" -> "1")
    val newUri = uri.replaceParams("testOne", "2")
    newUri.toString should equal ("/uris-in-scala.html?testOne=2")
  }

  "Replace param method" should "replace multiple parameters with a String argument" in {
    val uri = "/uris-in-scala.html" ? ("testOne" -> "1") & ("testOne" -> "2")
    val newUri = uri.replaceParams("testOne", "2")
    newUri.toString should equal ("/uris-in-scala.html?testOne=2")
  }

  "Replace param method" should "replace parameters with a Some argument" in {
    val uri = "/uris-in-scala.html" ? ("testOne" -> "1")
    val newUri = uri.replaceParams("testOne", Some("2"))
    newUri.toString should equal ("/uris-in-scala.html?testOne=2")
  }

  "Replace param method" should "remove parameters with a None argument" in {
    val uri = "/uris-in-scala.html" ? ("testOne" -> "1")
    val newUri = uri.replaceParams("testOne", None)
    newUri.toString should equal ("/uris-in-scala.html")
  }

  "Replace param method" should "not affect other parameters" in {
    val uri = "/uris-in-scala.html" ? ("testOne" -> "1") & ("testTwo" -> "2")
    val newUri = uri.replaceParams("testOne", "3")
    newUri.toString should equal ("/uris-in-scala.html?testOne=3&testTwo=2")
  }

  "Remove param method" should "remove multiple parameters" in {
    val uri = "/uris-in-scala.html" ? ("testOne" -> "1") & ("testOne" -> "2")
    val newUri = uri.removeParams("testOne")
    newUri.toString should equal ("/uris-in-scala.html")
  }

  "Remove param method" should "remove single parameters" in {
    val uri = "/uris-in-scala.html" ? ("testOne" -> "1")
    val newUri = uri.removeParams("testOne")
    newUri.toString should equal ("/uris-in-scala.html")
  }

  "Remove param method" should "not remove other parameters" in {
    val uri = "/uris-in-scala.html" ? ("testOne" -> "1") & ("testTwo" -> "2")
    val newUri = uri.removeParams("testOne")
    newUri.toString should equal ("/uris-in-scala.html?testTwo=2")
  }

  "Scheme setter method" should "copy the URI with the new scheme" in {
    val uri = "http://coldplay.com/chris-martin.html" ? ("testOne" -> "1")
    val newUri = uri.scheme("https")
    newUri.toString should equal ("https://coldplay.com/chris-martin.html?testOne=1")
  }

  "Host setter method" should "copy the URI with the new host" in {
    val uri = "http://coldplay.com/chris-martin.html" ? ("testOne" -> "1")
    val newUri = uri.host("jethrotull.com")
    newUri.toString should equal ("http://jethrotull.com/chris-martin.html?testOne=1")
  }

  "Port setter method" should "copy the URI with the new port" in {
    val uri = "http://coldplay.com/chris-martin.html" ? ("testOne" -> "1")
    val newUri = uri.port(8080)
    newUri.toString should equal ("http://coldplay.com:8080/chris-martin.html?testOne=1")
  }
}
