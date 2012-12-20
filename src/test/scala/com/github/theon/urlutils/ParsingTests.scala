package com.github.theon.urlutils

import org.scalatest._
import org.scalatest.matchers._
import com.github.theon.uri.Uri._
import com.github.theon.uri.UriParser

class ParsingTests extends FlatSpec with ShouldMatchers {

  "Parsing an absolute URI" should "result in a valid Uri object" in {
    val uri = parseUri("http://theon.github.com/uris-in-scala.html")
    uri.protocol should equal (Some("http"))
    uri.host should equal (Some("theon.github.com"))
    uri.path should equal ("/uris-in-scala.html")
  }

  "Parsing a relative URI" should "result in a valid Uri object" in {
    val uri = parseUri("/uris-in-scala.html")
    uri.protocol should equal (None)
    uri.host should equal (None)
    uri.path should equal ("/uris-in-scala.html")
  }

  "Parsing a URI with querystring paramteres" should "result in a valid Uri object" in {
    val uri = parseUri("/uris-in-scala.html?query_param_one=hello&query_param_one=goodbye&query_param_two=false")
    uri.query.params should equal (
      Map(
        ("query_param_two" -> List("false")),
        ("query_param_one" -> List("hello", "goodbye"))
      )
    )
  }
}