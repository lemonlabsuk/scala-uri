package io.lemonlabs.uri

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class PathTests extends AnyFlatSpec with Matchers {
  "UrlPath.fromRaw" should "create an EmptyPath" in {
    UrlPath.fromRaw("") should equal(EmptyPath)
  }

  it should "create an AbsolutePath" in {
    UrlPath.fromRaw("/a/b/c") should equal(AbsolutePath(Vector("a", "b", "c")))
  }

  it should "create a RootlessPath" in {
    UrlPath.fromRaw("a/b/c") should equal(RootlessPath(Vector("a", "b", "c")))
  }

  it should "not require reserved characters to be percent encoded" in {
    UrlPath.fromRaw("?/#/%") should equal(RootlessPath(Vector("?", "#", "%")))
  }
}
