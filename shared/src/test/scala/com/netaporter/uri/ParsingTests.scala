package com.netaporter.uri

import com.netaporter.uri.Uri._
import com.netaporter.uri.parsing._
import org.scalatest.{FlatSpec, Matchers}

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

  "Parsing a url with IPv4address" should "result in the correct host" in {
    val uri = parse("http://127.0.0.1:9000/uris-in-scala.html")
    uri.host should equal(Some("127.0.0.1"))
    uri.port should equal(Some(9000))
    uri.toString should equal ("http://127.0.0.1:9000/uris-in-scala.html")
  }

  "Parsing a url with IPv6address" should "result in the correct host" in {
    val uri = parse("http://[2001:db8::7]:9000/uris-in-scala.html")
    uri.host should equal(Some("[2001:db8::7]"))
    uri.port should equal(Some(9000))
    uri.toString should equal ("http://[2001:db8::7]:9000/uris-in-scala.html")
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
    val uri = parse("//theon.github.com#fragged")
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

  "Parsing a with user and empty password" should "result in a Uri with the user and empty password" in {
    val uri = parse("ftp://theon:@github.com")
    uri.scheme should equal(Some("ftp"))
    uri.user should equal(Some("theon"))
    uri.password should equal(Some(""))
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

  "Uri.parse" should "provide paramMap as a Map of String to Seq of String" in {
    val parsed = Uri.parse("/?a=b&a=c&d=&e&f&f=g")

    parsed.query.paramMap should be (Map(
      "a" -> Seq("b", "c"),
      "d" -> Seq(""),
      "e" -> Seq.empty,
      "f" -> Seq("g")
    ))
  }

  "Uri.parseQuery" should "parse a query string starting with a ?" in {
    val parsed = Uri.parseQuery("?a=b&c=d")
    parsed should equal(QueryString.create("a" -> Some("b"), "c" -> Some("d")))
  }

  it should "parse a query string not starting with a ?" in {
    val parsed = Uri.parseQuery("a=b&c=d")
    parsed should equal(QueryString.create("a" -> Some("b"), "c" -> Some("d")))
  }

  "mailto scheme" should "parse email address as the path" in {
    val mailto = Uri.parse("mailto:java-net@java.sun.com")
    mailto.scheme should equal(Some("mailto"))
    mailto.path should equal("java-net@java.sun.com")
  }

  "mailto scheme with query" should "parse email address as the path" in {
    val mailto = Uri.parse("mailto:someone@example.com?subject=Hello")
    mailto.scheme should equal(Some("mailto"))
    mailto.path should equal("someone@example.com")
    mailto.query.param("subject") should equal(Some("Hello"))
  }

  "URNs" should "parse everything after the scheme as the path" in {
    val urn = Uri.parse("urn:example:animal:ferret:nose")
    urn.scheme should equal(Some("urn"))
    urn.path should equal("example:animal:ferret:nose")
  }
}
