package io.lemonlabs.uri

import io.lemonlabs.uri.config.{ExcludeNones, UriConfig}
import io.lemonlabs.uri.typesafe.{Fragment, QueryKeyValue, QueryValue, TraversableParams, TraversablePathParts}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class TypeClassTests extends AnyFlatSpec with Matchers {
  "String" should "render correctly" in {
    val uri = Url.parse("/uris-in-scala.html").addParam("param" -> "hey")
    uri.toString should equal("/uris-in-scala.html?param=hey")
  }

  "Booleans" should "render correctly" in {
    val uri = Url.parse("/uris-in-scala.html").addParam("param" -> true)
    uri.toString should equal("/uris-in-scala.html?param=true")
  }

  "Integers" should "render correctly" in {
    val uri = Url.parse("/uris-in-scala.html").addParam("param" -> 1)
    uri.toString should equal("/uris-in-scala.html?param=1")
  }

  "Floats" should "render correctly" in {
    val uri = Url.parse("/uris-in-scala.html").addParam("param" -> 0.5f)
    uri.toString should equal("/uris-in-scala.html?param=0.5")
  }

  "Options" should "render correctly" in {
    val uri = Url
      .parse("/uris-in-scala.html")
      .addParam("param" -> Some("some"))
      .addParam("param2" -> None)
    uri.toString should equal("/uris-in-scala.html?param=some&param2")
  }

  "Vector" should "render correctly as pathParts" in {
    val uri = Url.parse("/a").addPathParts(Vector("b", "c", "d"))
    uri.toString should equal("/a/b/c/d")
  }

  "Iterable" should "render correctly as pathParts" in {
    val uri = Url.parse("/a").addPathParts(Iterable(true, false, true))
    uri.toString should equal("/a/true/false/true")
  }

  "Seq" should "render correctly as pathParts" in {
    val uri = Url.parse("/a").addPathParts(Seq(Some("b"), None, Some("c")))
    uri.toString should equal("/a/b//c")
  }

  "List" should "render correctly as pathParts" in {
    val uri = Url.parse("/a").addPathParts(List("b", "c", "d"))
    uri.toString should equal("/a/b/c/d")
  }

  "A single item" should "render correctly as pathParts" in {
    val uri = Url.parse("/a").addPathParts(5)
    uri.toString should equal("/a/5")
  }

  "Foo" should "render correctly as path part" in {
    final case class Foo(a: String, b: Int)
    object Foo {
      given pathPart: TraversablePathParts[Foo] = TraversablePathParts.product
    }

    val uri = Url.parse("http://example.com") addPathParts Foo(a = "user", b = 1)
    uri.toString should equal("http://example.com/user/1")
  }

  "Foo" should "render correctly as fragment" in {
    final case class Foo(a: String, b: Int)
    object Foo {
      given pathPart: Fragment[Foo] = (foo: Foo) => Some(s"${foo.a}-${foo.b}")
    }

    val uri = Url.parse("/uris-in-scala.html") withFragment Foo(a = "user", b = 1)
    uri.toString should equal("/uris-in-scala.html#user-1")
  }

  "Foo" should "render correctly as query parameters" in {
    final case class Foo(a: String)
    object Foo {
      given fooQueryKeyValue: QueryKeyValue[Foo] = QueryKeyValue(_ => "foo", foo => Option(foo.a))
    }

    val uri = Url.parse("/uris-in-scala.html") addParam Foo("foo_value")
    uri.toString should equal("/uris-in-scala.html?foo=foo_value")
  }

  "addPathParts(TraversablePathParts)" should "derive type class for case class correctly" in {
    final case class Foo(a: Int, b: String)

    object Foo {
      given traversablePathParts: TraversablePathParts[Foo] = TraversablePathParts.product
    }

    val uri = Url.parse("/uris-in-scala") addPathParts Foo(a = 1, b = "bar")
    uri.toString should equal("/uris-in-scala/1/bar")
  }

  "addPathParts(TraversablePathParts)" should "derive type class for case classes structure correctly" in {
    final case class Foo(a: Int, b: String)

    object Foo {
      given traversablePathParts: TraversablePathParts[Foo] = TraversablePathParts.product
    }

    final case class Bar(c: Int, foo: Foo)

    object Bar {
      given traversablePathParts: TraversablePathParts[Bar] = TraversablePathParts.product
    }

    val uri = Url.parse("/uris-in-scala") addPathParts Bar(c = 2, foo = Foo(a = 1, b = "bar"))
    uri.toString should equal("/uris-in-scala/2/1/bar")
  }

  "addPathParts(TraversablePathParts)" should "derive type class for case class with optional field correctly" in {
    final case class Foo(a: Int, b: Option[String])

    object Foo {
      given traversablePathParts: TraversablePathParts[Foo] = TraversablePathParts.product
    }

    val uriWithB = Url.parse("/uris-in-scala") addPathParts Foo(a = 1, b = Some("bar"))
    val uriWithoutB = Url.parse("/uris-in-scala") addPathParts Foo(a = 1, b = None)
    uriWithB.toString should equal("/uris-in-scala/1/bar")
    uriWithoutB.toString should equal("/uris-in-scala/1/")
  }

  "withPathParts(TraversablePathParts)" should "derive type class for case class correctly" in {
    final case class Foo(a: Int, b: String)

    object Foo {
      given traversablePathParts: TraversablePathParts[Foo] = TraversablePathParts.product
    }

    val uri = Url.parse("/uris-in-scala") withPathParts Foo(a = 1, b = "bar")
    uri.toString should equal("/1/bar")
  }

  "withPathParts(TraversablePathParts)" should "derive type class for case classes structure correctly" in {
    final case class Foo(a: Int, b: String)

    object Foo {
      given traversablePathParts: TraversablePathParts[Foo] = TraversablePathParts.product
    }

    final case class Bar(c: Int, foo: Foo)

    object Bar {
      given traversablePathParts: TraversablePathParts[Bar] = TraversablePathParts.product
    }

    val uri = Url.parse("/uris-in-scala") withPathParts Bar(c = 2, foo = Foo(a = 1, b = "bar"))
    uri.toString should equal("/2/1/bar")
  }

  "withPathParts(TraversablePathParts)" should "derive type class for case class with optional field correctly" in {
    final case class Foo(a: Int, b: Option[String])

    object Foo {
      given traversablePathParts: TraversablePathParts[Foo] = TraversablePathParts.product
    }

    val uriWithB = Url.parse("/uris-in-scala") withPathParts Foo(a = 1, b = Some("bar"))
    val uriWithoutB = Url.parse("/uris-in-scala") withPathParts Foo(a = 1, b = None)
    uriWithB.toString should equal("/1/bar")
    uriWithoutB.toString should equal("/1/")
  }

  "Vector" should "render correctly as params" in {
    val uri = Url.parse("/path").addParams(Vector("a" -> "b", "c" -> "d"))
    uri.toString should equal("/path?a=b&c=d")
  }

  "Iterable" should "render correctly as params" in {
    val uri = Url.parse("/path").addParams(Iterable(true -> false))
    uri.toString should equal("/path?true=false")
  }

  "Seq" should "render correctly as params" in {
    val uri = Url.parse("/path").addParams(Seq("a" -> None, "b" -> Some("c")))
    uri.toString should equal("/path?a&b=c")
  }

  "List" should "render correctly as params" in {
    val uri = Url.parse("/path").addParams(List("a" -> "b", "c" -> "d"))
    uri.toString should equal("/path?a=b&c=d")
  }

  "A single item" should "render correctly as params" in {
    val uri = Url.parse("/path").addParams("num" -> 5)
    uri.toString should equal("/path?num=5")
  }

  "TraversableParams" should "derive type class for case class correctly" in {
    final case class Foo(a: Int, b: String)

    object Foo {
      given traversableParams: TraversableParams[Foo] = TraversableParams.product
    }

    val uri = Url.parse("/uris-in-scala.html") addParams Foo(a = 1, b = "bar")
    uri.toString should equal("/uris-in-scala.html?a=1&b=bar")
  }

  "TraversableParams" should "derive type class for case classes structure correctly" in {
    final case class Foo(a: Int, b: String)

    object Foo {
      given traversableParams: TraversableParams[Foo] = TraversableParams.product
    }

    final case class Bar(c: Int, foo: Foo)

    object Bar {
      given traversableParams: TraversableParams[Bar] = TraversableParams.product
    }

    val uri = Url.parse("/uris-in-scala.html") addParams Bar(c = 2, foo = Foo(a = 1, b = "bar"))
    uri.toString should equal("/uris-in-scala.html?c=2&a=1&b=bar")
  }

  "TraversableParams" should "derive type class for case class with optional field correctly" in {
    final case class Foo(a: Int, b: Option[String])

    object Foo {
      given traversableParams: TraversableParams[Foo] = TraversableParams.product
    }

    val uriWithB = Url.parse("/uris-in-scala.html") addParams Foo(a = 1, b = Some("bar"))
    val uriWithoutB = Url.parse("/uris-in-scala.html") addParams Foo(a = 1, b = None)
    uriWithB.toString should equal("/uris-in-scala.html?a=1&b=bar")
    uriWithoutB.toString should equal("/uris-in-scala.html?a=1&b")

    {
      given config: UriConfig = UriConfig(renderQuery = ExcludeNones)
      val uriWithBexludingNones = Url.parse("/uris-in-scala.html") addParams Foo(a = 1, b = Some("bar"))
      val uriWithoutBexludingNones = Url.parse("/uris-in-scala.html") addParams Foo(a = 1, b = None)
      uriWithBexludingNones.toString should equal("/uris-in-scala.html?a=1&b=bar")
      uriWithoutBexludingNones.toString should equal("/uris-in-scala.html?a=1")
    }
  }

  "QueryValue" should "derive type class for coproduct type correctly" in {
    sealed trait Foo {
      def name: String
    }

    case object A extends Foo {
      val name: String = "A"
    }

    case object B extends Foo {
      val name: String = "B"
    }

    object Foo {
      given queryValue: QueryValue[Foo] = QueryValue.derive[Foo].by(_.name)
    }

    val uriA = Url.parse("/uris-in-scala.html") addParam ("foo" -> A)
    val uriB = Url.parse("/uris-in-scala.html") addParam ("foo" -> B)
    uriA.toString should equal("/uris-in-scala.html?foo=A")
    uriB.toString should equal("/uris-in-scala.html?foo=B")
  }

  "removeParams(QueryKey,QueryKey,QueryKey*)" should "remove all specified params" in {
    val url = Url.parse("/?a=1&b=2&c=3&d=4&e=5").removeParams("a", "c", "d", "e")
    url.toString should equal("/?b=2")
  }

  "removeQueryString()" should "remove all params" in {
    val url = Url.parse("/?a=1&b=2&c=3&d=4&e=5").removeQueryString()
    url.toString should equal("/")
  }
}
