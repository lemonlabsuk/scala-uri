package io.lemonlabs.uri

import io.lemonlabs.uri.typesafe.QueryKeyValue
import org.scalatest.{FlatSpec, Matchers}

class TypesafeDslTypeTests extends FlatSpec with Matchers {

  import io.lemonlabs.uri.typesafe.dsl._

  "String" should "render correctly" in {
    val uri = "/uris-in-scala.html" ? ("param" -> "hey")
    uri.toString should equal ("/uris-in-scala.html?param=hey")
  }

  "Booleans" should "render correctly" in {
    val uri = "/uris-in-scala.html" ? ("param" -> true)
    uri.toString should equal ("/uris-in-scala.html?param=true")
  }

  "Integers" should "render correctly" in {
    val uri = "/uris-in-scala.html" ? ("param" -> 1)
    uri.toString should equal ("/uris-in-scala.html?param=1")
  }

  "Floats" should "render correctly" in {
    val uri = "/uris-in-scala.html" ? ("param" -> 0.5f)
    uri.toString should equal ("/uris-in-scala.html?param=0.5")
  }

  "Options" should "render correctly" in {
    val uri = "/uris-in-scala.html" ? ("param" -> Some("some")) & ("param2" -> None)
    uri.toString should equal ("/uris-in-scala.html?param=some&param2")
  }

  "Foo" should "render correctly" in {
    final case class Foo(a : String)
    object Foo {
      implicit val fooQueryKeyValue: QueryKeyValue[Foo] = new QueryKeyValue[Foo] {
        override def queryKey(a: Foo): String = "foo"

        override def queryValue(a: Foo): Option[String] = Option(a.a)
      }
    }

    val uri = "/uris-in-scala.html" ? Foo("foo_value")
    uri.toString should equal ("/uris-in-scala.html?foo=foo_value")
  }
}


