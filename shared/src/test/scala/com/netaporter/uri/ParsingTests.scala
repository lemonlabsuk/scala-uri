package com.netaporter.uri

import org.scalatest.{FlatSpec, Matchers}

class ParsingTests extends FlatSpec with Matchers {

  "Parsing an absolute URI" should "result in a valid Uri object" in {
    val url = Url.parse("http://theon.github.com/uris-in-scala.html")
    url.schemeOption should equal (Some("http"))
    url.hostOption should equal (Some("theon.github.com"))
    url.path.toString should equal ("/uris-in-scala.html")
  }

  "Parsing a relative URI" should "result in a valid Uri object" in {
    val url = Url.parse("/uris-in-scala.html")
    url.schemeOption should equal (None)
    url.hostOption should equal (None)
    url.path.toString should equal ("/uris-in-scala.html")
  }

  "Parsing a URI with querystring parameters" should "result in a valid Uri object" in {
    val url = Url.parse("/uris-in-scala.html?query_param_one=hello&query_param_one=goodbye&query_param_two=false")
    url.query.params should equal (
      Vector (
        "query_param_one" -> Some("hello"),
        "query_param_one" -> Some("goodbye"),
        "query_param_two" -> Some("false")
      )
    )
  }

  "Parsing a URI with not properly URL-encoded querystring parameters" should "result in a valid Uri object" in {
    val url = Url.parse("/uris-in-scala.html?query_param_one=hello=world&query_param_two=false")
    url.query.params should equal (
      Vector (
        "query_param_one" -> Some("hello=world"),
        "query_param_two" -> Some("false")
      )
    )
  }

  "Parsing a URI with a zero-length querystring parameter" should "result in a valid Uri object" in {
    val url = Url.parse("/uris-in-scala.html?query_param_one=&query_param_two=false")
    url.query.params should equal (
      Vector (
        "query_param_one" -> Some(""),
        "query_param_two" -> Some("false")
      )
    )
  }

  "Parsing a url with IPv4address" should "result in the correct host" in {
    val url = Url.parse("http://127.0.0.1:9000/uris-in-scala.html")
    url.hostOption should equal(Some("127.0.0.1"))
    url.port should equal(Some(9000))
    url.toString should equal ("http://127.0.0.1:9000/uris-in-scala.html")
  }

  "Parsing a url with IPv6address" should "result in the correct host" in {
    val url = Url.parse("http://[2001:db8::7]:9000/uris-in-scala.html")
    url.hostOption should equal(Some("[2001:db8::7]"))
    url.port should equal(Some(9000))
    url.toString should equal ("http://[2001:db8::7]:9000/uris-in-scala.html")
  }

  "Parsing a url with relative scheme" should "result in a Uri with None for scheme" in {
    val url = Url.parse("//theon.github.com/uris-in-scala.html")
    url.schemeOption should equal (None)
    url.toString should equal ("//theon.github.com/uris-in-scala.html")
  }

  "Parsing a url with relative scheme" should "result in the correct host" in {
    val url = Url.parse("//theon.github.com/uris-in-scala.html")
    url.hostOption should equal(Some("theon.github.com"))
  }

  "Parsing a url with relative scheme" should "result in the correct path" in {
    val url = Url.parse("//theon.github.com/uris-in-scala.html")
    url.path.parts should equal(Vector("uris-in-scala.html"))
  }

  "Parsing a url with a fragment" should "result in a Uri with Some for fragment" in {
    val url = Url.parse("//theon.github.com#fragged")
    url.fragment should equal (Some("fragged"))
  }

  "Parsing a url with a query string and fragment" should "result in a Uri with Some for fragment" in {
    val url = Url.parse("//theon.github.com/uris-in-scala.html?ham=true#fragged")
    url.fragment should equal (Some("fragged"))
  }

  "Parsing a url without a fragment" should "result in a Uri with None for fragment" in {
    val url = Url.parse("//theon.github.com/uris-in-scala.html")
    url.fragment should equal (None)
  }

  "Parsing a url without an empty fragment" should "result in a Uri with Some(empty string) for fragment" in {
    val url = Url.parse("//theon.github.com/uris-in-scala.html#")
    url.fragment should equal (Some(""))
  }

  "Parsing a url with user" should "result in a Uri with the username" in {
    val url = AbsoluteUrl.parse("http://theon@github.com")
    url.schemeOption should equal(Some("http"))
    url.user should equal(Some("theon"))
    url.hostOption should equal(Some("github.com"))
  }

  "Parsing a with user and password" should "result in a Uri with the user and password" in {
    val url = Url.parse("ftp://theon:password@github.com")
    url.schemeOption should equal(Some("ftp"))
    url.user should equal(Some("theon"))
    url.password should equal(Some("password"))
    url.hostOption should equal(Some("github.com"))
  }

  "Parsing a with user and empty password" should "result in a Uri with the user and empty password" in {
    val url = Url.parse("ftp://theon:@github.com")
    url.schemeOption should equal(Some("ftp"))
    url.user should equal(Some("theon"))
    url.password should equal(Some(""))
    url.hostOption should equal(Some("github.com"))
  }

  "Protocol relative url with authority" should "parse correctly" in {
    val url = Url.parse("//user:pass@www.mywebsite.com/index.html")
    url.schemeOption should equal(None)
    url.user should equal(Some("user"))
    url.password should equal(Some("pass"))
    url.subdomain should equal(Some("www"))
    url.hostOption should equal(Some("www.mywebsite.com"))
    url.path.parts should equal(Vector("index.html"))
  }

  "Url with @ in query string" should "parse correctly" in {
    val url = Url.parse("http://www.mywebsite.com?a=b@")
    url.schemeOption should equal(Some("http"))
    url.hostOption should equal (Some("www.mywebsite.com"))
  }

  "Query string param with hash as value" should "be parsed as fragment" in {
    val url = Url.parse("http://stackoverflow.com?q=#frag")
    url.query.params("q") should equal(Vector(Some("")))
    url.fragment should equal(Some("frag"))
  }

  "Parsing a url with a query string that doesn't have a value" should "not throw an exception" in {
    val url = Url.parse("//theon.github.com/uris-in-scala.html?ham")
    url.hostOption should equal(Some("theon.github.com"))
    url.query.params("ham") should equal(Vector(None))
    url.toString should equal("//theon.github.com/uris-in-scala.html?ham")

    val url2 = Url.parse("//cythrawll.github.com/scala-url.html?q=foo&ham")
    url2.hostOption should equal(Some("cythrawll.github.com"))
    url2.query.params("ham") should equal(Vector(None))
    url2.query.params("q") should equal(Vector(Some("foo")))
    url2.toString should equal("//cythrawll.github.com/scala-url.html?q=foo&ham")


    val url3 = Url.parse("//cythrawll.github.com/scala-url.html?ham&q=foo")
    url3.hostOption should equal(Some("cythrawll.github.com"))
    url3.query.params("ham") should equal(Vector(None))
    url3.query.params("q") should equal(Vector(Some("foo")))
    url3.toString should equal("//cythrawll.github.com/scala-url.html?ham&q=foo")
  }

  "Parsing a url with two query strings that doesn't have a value in different ways" should "work and preserve the difference" in {
    val url = Url.parse("//cythrawll.github.com/scala-url.html?ham&jam=&q=foo")
    url.hostOption should equal (Some("cythrawll.github.com"))
    url.query.params("ham") should equal(Vector(None))
    url.query.params("jam") should equal(Vector(Some("")))
    url.query.params("q")   should equal(Vector(Some("foo")))
    url.toString should equal("//cythrawll.github.com/scala-url.html?ham&jam=&q=foo")
  }

  it should "not be parsed by default" in {
    val url = Url.parse("http://stackoverflow.com/path;paramOne=value;paramTwo=value2/pathTwo;paramOne=value")
    url.path.parts should equal(Vector(
      "path;paramOne=value;paramTwo=value2",
      "pathTwo;paramOne=value"
    ))
  }

  "Empty path parts" should "be maintained during parsing" in {
    val url = Url.parse("http://www.example.com/hi//bye")
    url.toString should equal("http://www.example.com/hi//bye")
  }

  "exotic/reserved characters in query string" should "be decoded" in {
    val parsedQueryString = QueryString.parse("?weird%3D%26key=strange%25value&arrow=%E2%87%94")
    parsedQueryString.params("weird=&key") should equal(Vector(Some("strange%value")))
    parsedQueryString.params("arrow") should equal(Vector(Some("⇔")))
  }

  "exotic/reserved characters in user info" should "be decoded" in {
    val parsedUserInfo = UserInfo.parse("user%3A:p%40ssword%E2%87%94@")
    parsedUserInfo.user should equal(Some("user:"))
    parsedUserInfo.password should equal(Some("p@ssword⇔"))
  }

  "Uri.parse" should "provide paramMap as a Map of String to Vector of String" in {
    val parsed = Url.parse("/?a=b&a=c&d=&e&f&f=g")

    parsed.query.paramMap should be (Map(
      "a" -> Vector("b", "c"),
      "d" -> Vector(""),
      "e" -> Vector.empty,
      "f" -> Vector("g")
    ))
  }

  "Uri.parseQuery" should "parse a query string starting with a ?" in {
    val parsed = QueryString.parse("?a=b&c=d")
    parsed should equal(QueryString.fromPairs("a" -> "b", "c" -> "d"))
  }

  it should "parse a query string not starting with a ?" in {
    val parsed = QueryString.parse("a=b&c=d")
    parsed should equal(QueryString.fromPairs("a" -> "b", "c" -> "d"))
  }

  "mailto scheme" should "parse email address as the path" in {
    val mailto = Url.parse("mailto:java-net@java.sun.com")
    mailto.schemeOption should equal(Some("mailto"))
    mailto.path.toString should equal("java-net@java.sun.com")
  }

  "mailto scheme with query" should "parse email address as the path" in {
    val mailto = Url.parse("mailto:someone@example.com?subject=Hello")
    mailto.schemeOption should equal(Some("mailto"))
    mailto.path.toString should equal("someone@example.com")
    mailto.query.param("subject") should equal(Some("Hello"))
  }

  "URNs" should "parse everything after the scheme as the path" in {
    val urn = Uri.parse("urn:example:animal:ferret:nose")
    urn.schemeOption should equal(Some("urn"))
    urn.path.toString should equal("example:animal:ferret:nose")
  }
}
