package io.lemonlabs.uri

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class ResolutionTests extends AnyFlatSpec with Matchers {
  val examples = Seq(
    "g:h" -> "g:h",
    "g" -> "http://a/b/c/g",
    "./g" -> "http://a/b/c/g",
    "g/" -> "http://a/b/c/g/",
    "/g" -> "http://a/g",
    "//g" -> "http://g",
    "?y" -> "http://a/b/c/d;p?y",
    "g?y" -> "http://a/b/c/g?y",
    "#s" -> "http://a/b/c/d;p?q#s",
    "g#s" -> "http://a/b/c/g#s",
    "g?y#s" -> "http://a/b/c/g?y#s",
    ";x" -> "http://a/b/c/;x",
    "g;x" -> "http://a/b/c/g;x",
    "g;x?y#s" -> "http://a/b/c/g;x?y#s",
    "" -> "http://a/b/c/d;p?q",
    "." -> "http://a/b/c/",
    "./" -> "http://a/b/c/",
    ".." -> "http://a/b/",
    "../" -> "http://a/b/",
    "../g" -> "http://a/b/g",
    "../.." -> "http://a/",
    "../../" -> "http://a/",
    "../../g" -> "http://a/g",
    "../../../g" -> "http://a/g",
    "../../../../g" -> "http://a/g",
    "./../g" -> "http://a/b/g",
    "./g/." -> "http://a/b/c/g/",
    "g/./h" -> "http://a/b/c/g/h",
    "g/../h" -> "http://a/b/c/h",
    "g;x=1/./y" -> "http://a/b/c/g;x=1/y",
    "g;x=1/../y" -> "http://a/b/c/y",
    "g?y/./x" -> "http://a/b/c/g?y/./x",
    "g?y/../x" -> "http://a/b/c/g?y/../x",
    "g#s/./x" -> "http://a/b/c/g#s/./x",
    "g#s/../x" -> "http://a/b/c/g#s/../x"
  )

  "resolve" should "comply with all examples from RFC3986, section 5.4" in {
    val base = AbsoluteUrl.parse("http://a/b/c/d;p?q")
    examples.map { case (ref, expected) =>
      Url.parse(ref).resolve(base).toString should equal(expected)
    }
  }

  it should "make the reference URL's path absolute if the base URL has empty path and no authority" in {
    val base = AbsoluteUrl.parse("http://foo")
    Url.parse("a").resolve(base).toString should equal("http://foo/a")
  }
}
