package io.lemonlabs.uri

import io.lemonlabs.uri.config.{ExcludeNones, UriConfig}
import io.lemonlabs.uri.typesafe._
import shapeless._
import shapeless.labelled._
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class TypesafeDslTypeTests extends AnyFlatSpec with Matchers {
  import io.lemonlabs.uri.typesafe.dsl._

  "String" should "render correctly" in {
    val uri = "/uris-in-scala.html" ? ("param" -> "hey")
    uri.toString should equal("/uris-in-scala.html?param=hey")
  }

  "Booleans" should "render correctly" in {
    val uri = "/uris-in-scala.html" ? ("param" -> true)
    uri.toString should equal("/uris-in-scala.html?param=true")
  }

  "Integers" should "render correctly" in {
    val uri = "/uris-in-scala.html" ? ("param" -> 1)
    uri.toString should equal("/uris-in-scala.html?param=1")
  }

  "Floats" should "render correctly" in {
    val uri = "/uris-in-scala.html" ? ("param" -> 0.5f)
    uri.toString should equal("/uris-in-scala.html?param=0.5")
  }

  "Options" should "render correctly" in {
    val uri = "/uris-in-scala.html" ? ("param" -> Some("some")) & ("param2" -> None)
    uri.toString should equal("/uris-in-scala.html?param=some&param2")
  }

  "Foo" should "render correctly as path part" in {
    final case class Foo(a: String, b: Int)
    object Foo {
      implicit val pathPart: PathPart[Foo] = (foo: Foo) => s"${foo.a}/${foo.b}"
    }

    val uri = "/uris-in-scala.html" / Foo(a = "user", b = 1)
    uri.toString should equal("/uris-in-scala.html/user/1")
  }

  "Foo" should "render correctly as fragment" in {
    final case class Foo(a: String, b: Int)
    object Foo {
      implicit val pathPart: Fragment[Foo] = (foo: Foo) => s"${foo.a}-${foo.b}"
    }

    val uri = "/uris-in-scala.html" `#` Foo(a = "user", b = 1)
    uri.toString should equal("/uris-in-scala.html#user-1")
  }

  "Foo" should "render correctly as query parameters" in {
    final case class Foo(a: String)
    object Foo {
      implicit val fooQueryKeyValue: QueryKeyValue[Foo] = new QueryKeyValue[Foo] {
        override def queryKey(a: Foo): String = "foo"

        override def queryValue(a: Foo): Option[String] = Option(a.a)
      }
    }

    val uri = "/uris-in-scala.html" ? Foo("foo_value")
    uri.toString should equal("/uris-in-scala.html?foo=foo_value")
  }

  "TraversableParams" should "derive type class for case class correctly" in {
    final case class Foo(a: Int, b: String)

    object Foo {
      implicit val traversableParams: TraversableParams[Foo] = TraversableParams.product
    }

    val uri = "/uris-in-scala.html" withParams Foo(a = 1, b = "bar")
    uri.toString should equal("/uris-in-scala.html?a=1&b=bar")
  }

  "TraversableParams" should "derive type class for case classes structure correctly" in {
    final case class Foo(a: Int, b: String)

    object Foo {
      implicit val traversableParams: TraversableParams[Foo] = TraversableParams.product
    }

    final case class Bar(c: Int, foo: Foo)

    object Bar {
      implicit val traversableParams: TraversableParams[Bar] = TraversableParams.product
    }

    val uri = "/uris-in-scala.html" withParams Bar(c = 2, foo = Foo(a = 1, b = "bar"))
    uri.toString should equal("/uris-in-scala.html?c=2&a=1&b=bar")
  }

  "TraversableParams" should "derive type class for case class with optional field correctly" in {
    final case class Foo(a: Int, b: Option[String])

    object Foo {
      implicit val traversableParams: TraversableParams[Foo] = TraversableParams.product
    }

    val uriWithB = "/uris-in-scala.html" withParams Foo(a = 1, b = Some("bar"))
    val uriWithoutB = "/uris-in-scala.html" withParams Foo(a = 1, b = None)
    uriWithB.toString should equal("/uris-in-scala.html?a=1&b=bar")
    uriWithoutB.toString should equal("/uris-in-scala.html?a=1&b")

    {
      implicit val config: UriConfig = UriConfig(renderQuery = ExcludeNones)
      val uriWithBexludingNones = "/uris-in-scala.html" withParams Foo(a = 1, b = Some("bar"))
      val uriWithoutBexludingNones = "/uris-in-scala.html" withParams Foo(a = 1, b = None)
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
      implicit val queryValue: QueryValue[Foo] = QueryValue.derive[Foo].by(_.name)
    }

    val uriA = "/uris-in-scala.html" ? ("foo" -> A)
    val uriB = "/uris-in-scala.html" ? ("foo" -> B)
    uriA.toString should equal("/uris-in-scala.html?foo=A")
    uriB.toString should equal("/uris-in-scala.html?foo=B")
  }
}
