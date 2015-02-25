package com.netaporter.uri

import org.scalatest.{Matchers, FlatSpec}
import Uri._
import scala._
import scala.Some
import com.netaporter.uri.parsing._
import com.netaporter.uri.config.UriConfig

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
        ("query_param_one" -> Some("hello")),
        ("query_param_one" -> Some("goodbye")),
        ("query_param_two" -> Some("false"))
      )
    )
  }

  "Parsing a URI with not properly URL-encoded querystring parameters" should "result in a valid Uri object" in {
    val uri = parse("/uris-in-scala.html?query_param_one=hello=world&query_param_two=false")
    uri.query.params should equal (
      Vector (
        ("query_param_one" -> Some("hello=world")),
        ("query_param_two" -> Some("false"))
      )
    )
  }

  "Parsing a URI with a zero-length querystring parameter" should "result in a valid Uri object" in {
    val uri = parse("/uris-in-scala.html?query_param_one=&query_param_two=false")
    uri.query.params should equal (
      Vector (
        ("query_param_one" -> Some("")),
        ("query_param_two" -> Some("false"))
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

  "Url with @ in query string" should "parse correctly" in {
    val uri = parse("http://www.mywebsite.com?a=b@")
    uri.scheme should equal(Some("http"))
    uri.host should equal (Some("www.mywebsite.com"))
  }

  "Query string param with hash as value" should "be parsed as fragment" in {
    val uri = parse("http://stackoverflow.com?q=#frag")
    uri.query.params("q") should equal(Vector(Some("")))
    uri.fragment should equal(Some("frag"))
  }

  "Parsing a url with a query string that doesn't have a value" should "not throw an exception" in {
    val uri = parse("//theon.github.com/uris-in-scala.html?ham")
    uri.host should equal(Some("theon.github.com"))
    uri.query.params("ham") should equal(Vector(None))
    uri.toString should equal("//theon.github.com/uris-in-scala.html?ham")

    val uri2 = parse("//cythrawll.github.com/scala-uri.html?q=foo&ham")
    uri2.host should equal(Some("cythrawll.github.com"))
    uri2.query.params("ham") should equal(Vector(None))
    uri2.query.params("q") should equal(Vector(Some("foo")))
    uri2.toString should equal("//cythrawll.github.com/scala-uri.html?q=foo&ham")


    val uri3 = parse("//cythrawll.github.com/scala-uri.html?ham&q=foo")
    uri3.host should equal(Some("cythrawll.github.com"))
    uri3.query.params("ham") should equal(Vector(None))
    uri3.query.params("q") should equal(Vector(Some("foo")))
    uri3.toString should equal("//cythrawll.github.com/scala-uri.html?ham&q=foo")
  }

  "Parsing a url with two query strings that doesn't have a value in different ways" should "work and preserve the difference" in {
    val uri4 = parse("//cythrawll.github.com/scala-uri.html?ham&jam=&q=foo")
    uri4.host should equal (Some("cythrawll.github.com"))
    uri4.query.params("ham") should equal(Vector(None))
    uri4.query.params("jam") should equal(Vector(Some("")))
    uri4.query.params("q")   should equal(Vector(Some("foo")))
    uri4.toString should equal("//cythrawll.github.com/scala-uri.html?ham&jam=&q=foo")
  }


  "Path with matrix params" should "be parsed when configured" in {
    implicit val config = UriConfig(matrixParams = true)
    val uri = parse("http://stackoverflow.com/path;paramOne=value;paramTwo=value2/pathTwo;paramOne=value")
    uri.pathParts should equal(Vector(
      MatrixParams("path", Vector("paramOne" -> Some("value"), "paramTwo" -> Some("value2"))),
      MatrixParams("pathTwo", Vector("paramOne" -> Some("value")))
    ))
  }

  it should "not be parsed by default" in {
    val uri = parse("http://stackoverflow.com/path;paramOne=value;paramTwo=value2/pathTwo;paramOne=value")
    uri.pathParts should equal(Vector(
      StringPathPart("path;paramOne=value;paramTwo=value2"),
      StringPathPart("pathTwo;paramOne=value")
    ))
  }

  "Empty path parts" should "be maintained during parsing" in {
    val uri = parse("http://www.example.com/hi//bye")
    uri.toString should equal("http://www.example.com/hi//bye")
  }

  "exotic/reserved characters in query string" should "be decoded" in {
    val q = "?weird%3D%26key=strange%25value&arrow=%E2%87%94"
    val parsedQueryString = new DefaultUriParser(q, config.UriConfig.default)._queryString.run().get
    parsedQueryString.params("weird=&key") should equal(Seq(Some("strange%value")))
    parsedQueryString.params("arrow") should equal(Seq(Some("⇔")))
  }

  "exotic/reserved characters in user info" should "be decoded" in {
    val userInfo = "user%3A:p%40ssword%E2%87%94@"
    val parsedUserInfo = new DefaultUriParser(userInfo, config.UriConfig.default)._userInfo.run().get
    parsedUserInfo.user should equal("user:")
    parsedUserInfo.pass should equal(Some("p@ssword⇔"))
  }
}