package com.netaporter.uri

import org.scalatest.{Matchers, FlatSpec}
import Uri._
import scala._
import scala.Some

class ParsingTests extends FlatSpec with Matchers {

  "Parsing an absolute URI" should "result in a valid Uri object" in {
    val uri = parse("http://theon.github.com/uris-in-scala.html")
    uri.scheme should equal (Some("http"))
    uri.host should equal (Some("theon.github.com"))
    uri.path should equal ("/uris-in-scala.html")
  }

  "Parsing a relative URI" should "result in a valid Uri object" in {
    val uri = parse("/uris-in-scala.html")
    uri.scheme should equal (None)
    uri.host should equal (None)
    uri.path should equal ("/uris-in-scala.html")
  }

  "Parsing a URI with querystring parameters" should "result in a valid Uri object" in {
    val uri = parse("/uris-in-scala.html?query_param_one=hello&query_param_one=goodbye&query_param_two=false")
    uri.query.params should equal (
      Vector (
        ("query_param_one" -> "hello"),
        ("query_param_one" -> "goodbye"),
        ("query_param_two" -> "false")
      )
    )
  }

  "Parsing a url with relative scheme" should "result in a Uri with None for scheme" in {
    val uri = parse("//theon.github.com/uris-in-scala.html")
    uri.scheme should equal (None)
    uri.toString should equal ("//theon.github.com/uris-in-scala.html")
  }

  "Parsing a url with relative scheme" should "result in the correct host" in {
    val uri = parse("//theon.github.com/uris-in-scala.html")
    uri.host should equal(Some("theon.github.com"))
  }

  "Parsing a url with relative scheme" should "result in the correct path" in {
    val uri = parse("//theon.github.com/uris-in-scala.html")
    uri.pathParts should equal(Vector(PathPart("uris-in-scala.html")))
  }

  "Parsing a url with a fragment" should "result in a Uri with Some for fragment" in {
    val uri = parse("//theon.github.com/uris-in-scala.html#fragged")
    uri.fragment should equal (Some("fragged"))
  }

  "Parsing a url with a query string and fragment" should "result in a Uri with Some for fragment" in {
    val uri = parse("//theon.github.com/uris-in-scala.html?ham=true#fragged")
    uri.fragment should equal (Some("fragged"))
  }

  "Parsing a url without a fragment" should "result in a Uri with None for fragment" in {
    val uri = parse("//theon.github.com/uris-in-scala.html")
    uri.fragment should equal (None)
  }

  "Parsing a url without an empty fragment" should "result in a Uri with Some(empty string) for fragment" in {
    val uri = parse("//theon.github.com/uris-in-scala.html#")
    uri.fragment should equal (Some(""))
  }

  "Parsing a url with user" should "result in a Uri with the username" in {
    val uri = parse("mailto://theon@github.com")
    uri.scheme should equal(Some("mailto"))
    uri.user should equal(Some("theon"))
    uri.host should equal(Some("github.com"))
  }

  "Parsing a with user and password" should "result in a Uri with the user and password" in {
    val uri = parse("ftp://theon:password@github.com")
    uri.scheme should equal(Some("ftp"))
    uri.user should equal(Some("theon"))
    uri.password should equal(Some("password"))
    uri.host should equal(Some("github.com"))
  }

  "Protocol relative url with authority" should "parse correctly" in {
    val uri = parse("//user:pass@www.mywebsite.com/index.html")
    uri.scheme should equal(None)
    uri.user should equal(Some("user"))
    uri.password should equal(Some("pass"))
    uri.subdomain should equal(Some("www"))
    uri.host should equal(Some("www.mywebsite.com"))
    uri.pathParts should equal(Vector(PathPart("index.html")))
  }

  "Query string param with hash as value" should "be parsed as fragment" in {
    val uri = parse("http://stackoverflow.com?q=#frag")
    uri.query.params("q") should equal(Vector(""))
    uri.fragment should equal(Some("frag"))
  }

  "Path with matrix params" should "be parsed" in {
    val uri = parse("http://stackoverflow.com/path;paramOne=value;paramTwo=value2/pathTwo;paramOne=value")
    uri.pathParts should equal(Vector(
      MatrixParams("path", Vector("paramOne" -> "value", "paramTwo" -> "value2")),
      MatrixParams("pathTwo", Vector("paramOne" -> "value"))
    ))
  }

  "Empty path parts" should "be maintained during parsing" in {
    val uri = parse("http://www.example.com/hi//bye")
    uri.toString should equal("http://www.example.com/hi//bye")
  }

}