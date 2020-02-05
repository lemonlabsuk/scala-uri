package io.lemonlabs.uri

import io.lemonlabs.uri.config.{All, ExcludeNones, UriConfig}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class TypesafeDslTests extends AnyFlatSpec with Matchers {
  import io.lemonlabs.uri.typesafe.dsl._

  "A simple absolute URI" should "render correctly" in {
    val uri: Uri = "http://theon.github.com/uris-in-scala.html"
    uri.toString should equal("http://theon.github.com/uris-in-scala.html")
  }

  "A simple relative URI" should "render correctly" in {
    val uri: Uri = "/uris-in-scala.html"
    uri.toString should equal("/uris-in-scala.html")
  }

  "An absolute URI with query string parameters" should "render correctly" in {
    val uri = "http://theon.github.com/uris-in-scala.html" ? ("testOne" -> "1") & ("testTwo" -> "2")
    uri.toString should equal("http://theon.github.com/uris-in-scala.html?testOne=1&testTwo=2")
  }

  "A relative URI with query string parameters" should "render correctly" in {
    val uri = "/uris-in-scala.html" ? ("testOne" -> "1") & ("testTwo" -> "2")
    uri.toString should equal("/uris-in-scala.html?testOne=1&testTwo=2")
  }

  "A relative URI with query string Tuple2 parameter" should "render correctly" in {
    val uri = "/uris-in-scala.html" ? ("testOne", "1")
    uri.toString should equal("/uris-in-scala.html?testOne=1")
  }

  "A relative URI with multiple query string Tuple2 parameters" should "render correctly" in {
    val uri = "/uris-in-scala.html" ? ("testOne", "1") & ("testTwo", 2)
    uri.toString should equal("/uris-in-scala.html?testOne=1&testTwo=2")
  }

  "Multiple query string parameters with the same name" should "render correctly" in {
    val uri = "/uris-in-scala.html" ? ("testOne" -> "1") & ("testOne" -> "2")
    uri.toString should equal("/uris-in-scala.html?testOne=1&testOne=2")
  }

  "Query string parameters with value None" should "not be rendered with renderQuery=ExcludeNones" in {
    implicit val config: UriConfig = UriConfig(renderQuery = ExcludeNones)
    val uri = "/uris-in-scala.html" ? ("testOne" -> None) & ("testTwo" -> "2") & ("testThree" -> None)
    uri.toString should equal("/uris-in-scala.html?testTwo=2")
  }

  "Single Query string parameter with value None" should "not be rendered with renderQuery=ExcludeNones" in {
    implicit val config: UriConfig = UriConfig(renderQuery = ExcludeNones)
    val uri = "/uris-in-scala.html" ? ("testOne" -> None)
    uri.toString should equal("/uris-in-scala.html")
  }

  "Query string parameters with value None" should "be rendered with renderQuery=All" in {
    implicit val config: UriConfig = UriConfig(renderQuery = All)
    val uri = "/uris-in-scala.html" ? ("testOne" -> None) & ("testTwo" -> "2") & ("testThree" -> None)
    uri.toString should equal("/uris-in-scala.html?testOne&testTwo=2&testThree")
  }

  "Replace param method" should "replace single parameters with a String argument" in {
    val uri = "/uris-in-scala.html" ? ("testOne" -> "1")
    val newUri = uri.replaceParams("testOne", "2")
    newUri.toString should equal("/uris-in-scala.html?testOne=2")
  }

  "Replace param method" should "replace multiple parameters with a String argument" in {
    val uri = "/uris-in-scala.html" ? ("testOne" -> "1") & ("testOne" -> "2")
    val newUri = uri.replaceParams("testOne", "2")
    newUri.toString should equal("/uris-in-scala.html?testOne=2")
  }

  "Replace param method" should "replace parameters with a Some argument" in {
    val uri = "/uris-in-scala.html" ? ("testOne" -> "1")
    val newUri = uri.replaceParams("testOne", Some("2"))
    newUri.toString should equal("/uris-in-scala.html?testOne=2")
  }

  "Replace param method" should "not affect other parameters" in {
    val uri = "/uris-in-scala.html" ? ("testOne" -> "1") & ("testTwo" -> "2")
    val newUri = uri.replaceParams("testOne", "3")
    newUri.toString should equal("/uris-in-scala.html?testTwo=2&testOne=3")
  }

  "Remove param method" should "remove multiple parameters" in {
    val uri = "/uris-in-scala.html" ? ("testOne" -> "1") & ("testOne" -> "2")
    val newUri = uri.removeParams("testOne")
    newUri.toString should equal("/uris-in-scala.html")
  }

  "withQueryString" should "replace all query params" in {
    val uri = "/uris-in-scala.html" ? ("testOne" -> "1") & ("testTwo" -> "2")
    val newUri = uri.withQueryString("testThree" -> Some("3"), "testFour" -> Some("4"))
    newUri.toString should equal("/uris-in-scala.html?testThree=3&testFour=4")
  }

  "Remove param method" should "remove single parameters" in {
    val uri = "/uris-in-scala.html" ? ("testOne" -> "1")
    val newUri = uri.removeParams("testOne")
    newUri.toString should equal("/uris-in-scala.html")
  }

  "Remove param method" should "not remove other parameters" in {
    val uri = "/uris-in-scala.html" ? ("testOne" -> "1") & ("testTwo" -> "2")
    val newUri = uri.removeParams("testOne")
    newUri.toString should equal("/uris-in-scala.html?testTwo=2")
  }

  "Remove param method" should "remove parameters contained in SeqLike" in {
    val uri = "/uris-in-scala.html" ? ("testOne" -> "1") & ("testTwo" -> "2")
    val newUri = uri.removeParams(List("testOne", "testTwo"))
    newUri.toString should equal("/uris-in-scala.html")
  }

  "Remove param method" should "not remove parameters uncontained in List" in {
    val uri = "/uris-in-scala.html" ? ("testOne" -> "1") & ("testTwo" -> "2")
    val newUri = uri.removeParams(List("testThree", "testFour"))
    newUri.toString should equal("/uris-in-scala.html?testOne=1&testTwo=2")
  }

  "Remove param method" should "remove parameters contained in List and not remove parameters uncontained in List" in {
    val uri = "/uris-in-scala.html" ? ("testOne" -> "1") & ("testTwo" -> "2")
    val newUri = uri.removeParams(List("testOne", "testThree"))
    newUri.toString should equal("/uris-in-scala.html?testTwo=2")
  }

  "with empty QueryString" should "remove all query params" in {
    val uri = "/uris-in-scala.html" ? ("testOne" -> "1") & ("testTwo" -> "2")
    val newUri = uri.withQueryString(QueryString.empty)
    newUri.toString should equal("/uris-in-scala.html")
  }

  "Scheme setter method" should "copy the URI with the new scheme" in {
    val uri = "http://coldplay.com/chris-martin.html" ? ("testOne" -> "1")
    val newUri = uri.withScheme("https")
    newUri.toString should equal("https://coldplay.com/chris-martin.html?testOne=1")
  }

  "Host setter method" should "copy the URI with the new host" in {
    val uri = "http://coldplay.com/chris-martin.html" ? ("testOne" -> "1")
    val newUri = uri.withHost("jethrotull.com")
    newUri.toString() should equal("http://jethrotull.com/chris-martin.html?testOne=1")
  }

  "Port setter method" should "copy the URI with the new port" in {
    val uri = "http://coldplay.com/chris-martin.html" ? ("testOne" -> "1")
    val newUri = uri.toAbsoluteUrl.withPort(8080)
    newUri.toString() should equal("http://coldplay.com:8080/chris-martin.html?testOne=1")
  }

  "Path with fragment" should "render correctly" in {
    val uri = "http://google.com/test" `#` "fragment"
    uri.toString should equal("http://google.com/test#fragment")
  }

  "Path with query string and fragment" should "render correctly" in {
    val uri = "http://google.com/test" ? ("q" -> "scala-uri") `#` "fragment"
    uri.toString should equal("http://google.com/test?q=scala-uri#fragment")
  }

  "Uri with user info" should "render correctly" in {
    val uri = "http://user:password@moonpig.com/" `#` "hi"
    uri.toString should equal("http://user:password@moonpig.com/#hi")
  }

  "Uri with a changed user" should "render correctly" in {
    val uri = "http://user:password@moonpig.com/" `#` "hi"
    uri.toAbsoluteUrl.withUser("ian").toString() should equal("http://ian:password@moonpig.com/#hi")
  }

  "Uri with a changed password" should "render correctly" in {
    val uri = "http://user:password@moonpig.com/" `#` "hi"
    uri.toAbsoluteUrl.withPassword("not-so-secret").toString() should equal("http://user:not-so-secret@moonpig.com/#hi")
  }

  "A sequence of query params" should "get added successfully" in {
    val uri = "http://example.com".addParams(("name", true), ("key2", false))
    uri.query.params("name") should equal(Some("true") :: Nil)
    uri.query.params("key2") should equal(Some("false") :: Nil)
    uri.toString should equal("http://example.com?name=true&key2=false")
  }

  "A list of query params" should "get added successfully" in {
    val uri = "http://example.com".addParams(List(("name", true), ("key2", false)))
    uri.query.params("name") should equal(Some("true") :: Nil)
    uri.query.params("key2") should equal(Some("false") :: Nil)
    uri.toString should equal("http://example.com?name=true&key2=false")
  }

  "A map of query params" should "get added successfully" in {
    val p = Map("name" -> true, "key2" -> false)
    val uri = "http://example.com".addParams(p)
    uri.query.params("name") should equal(Some("true") :: Nil)
    uri.query.params("key2") should equal(Some("false") :: Nil)
    uri.toString should equal("http://example.com?name=true&key2=false")
  }

  "A list of query params" should "get added to a URL already with query params successsfully" in {
    val p = ("name", true) :: ("key2", false) :: Nil
    val uri = ("http://example.com" ? ("name" -> Some("param1"))).addParams(p)
    uri.query.params("name") should equal(Vector(Some("param1"), Some("true")))
    uri.query.params("key2") should equal(Some("false") :: Nil)
  }

  "A map of query params" should "get added to a URL already with query params successsfully" in {
    val p = Map("name" -> true, "key2" -> false)
    val uri = ("http://example.com" ? ("name" -> Some("param1"))).addParams(p)
    uri.query.params("name") should equal(Vector(Some("param1"), Some("true")))
    uri.query.params("key2") should equal(Some("false") :: Nil)
  }

  "Path and query DSL" should "be possible to use together" in {
    val uri = "http://host" / "path" / "to" / "resource" ? ("a" -> "1") & ("b" -> "2")
    uri.toString should equal("http://host/path/to/resource?a=1&b=2")
  }

  "Path and fragment DSL" should "be possible to use together" in {
    val uri = "http://host" / "path" / "to" / "resource" `#` "hellyeah"
    uri.toString should equal("http://host/path/to/resource#hellyeah")
  }

  "Path and query and fragment DSL" should "be possible to use together" in {
    val uri = "http://host" / "path" / "to" / "resource" ? ("a" -> "1") & ("b" -> "2") `#` "wow"
    uri.toString should equal("http://host/path/to/resource?a=1&b=2#wow")
  }

  "Latter fragments DSLs" should "overwrite earlier fragments" in {
    val uri = "http://host" / "path" / "to" `#` "weird" / "resource" ? ("a" -> "1") & ("b" -> "2") `#` "wow"
    uri.toString should equal("http://host/path/to/resource?a=1&b=2#wow")
  }

  "/? operator" should "add a slash to the path and a tuple query param" in {
    val uri = "http://host" /? ("a" -> "1")
    uri.toString should equal("http://host/?a=1")
  }

  "/? operator" should "add a slash to the path and a query param" in {
    val uri = "http://host" /? ("a", "1")
    uri.toString should equal("http://host/?a=1")
  }

  "&& operator" should "add a tuple with Some value" in {
    val uri = "http://host" && ("a" -> Some("1"))
    uri.toString should equal("http://host?a=1")
  }

  "&& operator" should "add a Some value" in {
    val uri = "http://host" && ("a", Some("1"))
    uri.toString should equal("http://host?a=1")
  }

  "&& operator" should "not add a tuple with None value" in {
    val uri = "http://host" && ("a" -> None)
    uri.toString should equal("http://host")
  }

  "&& operator" should "not add a None value" in {
    val uri = "http://host" && ("a", None)
    uri.toString should equal("http://host")
  }

  "? operator" should "merge Urls" in {
    val uri = "http://host" ? Url.parse("?a=b")
    uri.query.params should equal(Vector("a" -> Some("b")))
    uri.toString should equal("http://host?a=b")
  }

  "# operator" should "merge Urls" in {
    val uri = "http://host" ? Url.parse("#a")
    uri.fragment should equal(Some("a"))
    uri.toString should equal("http://host#a")
  }

  it should "work alongside the / operator" in {
    val uri = "http://host" / "path" /? ("a" -> "1")
    uri.toString should equal("http://host/path/?a=1")
  }

  it should "work alongside the & operator" in {
    val uri = "http://host" /? ("a" -> "1") & ("b" -> "2")
    uri.toString should equal("http://host/?a=1&b=2")
  }

  it should "work alongside the / and & operators together" in {
    val uri = "http://host" / "path" /? ("a" -> "1") & ("b" -> "2")
    uri.toString should equal("http://host/path/?a=1&b=2")
  }
}
