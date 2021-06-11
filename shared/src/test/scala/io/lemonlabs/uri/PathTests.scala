package io.lemonlabs.uri

import io.lemonlabs.uri.Path.SlashTermination
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

  "Url.slashTerminate" should "add or remove trailing slashes in the path" in {
    AbsoluteUrl.parse("https://a.com").slashTerminated(SlashTermination.None).toString() should
      equal("https://a.com")
    AbsoluteUrl.parse("https://a.com").slashTerminated(SlashTermination.EmptyPath).toString() should
      equal("https://a.com/")
    AbsoluteUrl.parse("https://a.com").slashTerminated(SlashTermination.EmptyPathOnly).toString() should
      equal("https://a.com/")
    AbsoluteUrl.parse("https://a.com").slashTerminated(SlashTermination.All).toString() should
      equal("https://a.com/")
    AbsoluteUrl.parse("https://a.com/").slashTerminated(SlashTermination.None).toString() should
      equal("https://a.com")
    AbsoluteUrl.parse("https://a.com/").slashTerminated(SlashTermination.EmptyPath).toString() should
      equal("https://a.com/")
    AbsoluteUrl.parse("https://a.com/").slashTerminated(SlashTermination.EmptyPathOnly).toString() should
      equal("https://a.com/")
    AbsoluteUrl.parse("https://a.com/").slashTerminated(SlashTermination.All).toString() should
      equal("https://a.com/")
    AbsoluteUrl.parse("https://a.com/b").slashTerminated(SlashTermination.None).toString() should
      equal("https://a.com/b")
    AbsoluteUrl.parse("https://a.com/b").slashTerminated(SlashTermination.EmptyPath).toString() should
      equal("https://a.com/b")
    AbsoluteUrl.parse("https://a.com/b").slashTerminated(SlashTermination.EmptyPathOnly).toString() should
      equal("https://a.com/b")
    AbsoluteUrl.parse("https://a.com/b").slashTerminated(SlashTermination.All).toString() should
      equal("https://a.com/b/")
    AbsoluteUrl.parse("https://a.com/b/").slashTerminated(SlashTermination.None).toString() should
      equal("https://a.com/b")
    AbsoluteUrl.parse("https://a.com/b/").slashTerminated(SlashTermination.EmptyPath).toString() should
      equal("https://a.com/b/")
    AbsoluteUrl.parse("https://a.com/b/").slashTerminated(SlashTermination.EmptyPathOnly).toString() should
      equal("https://a.com/b")
    AbsoluteUrl.parse("https://a.com/b/").slashTerminated(SlashTermination.All).toString() should
      equal("https://a.com/b/")

  }

  "Url.removeEmptyPathParts" should "remove empty parts in the path" in {
    AbsoluteUrl.parse("https://a.com").removeEmptyPathParts.toString() should equal("https://a.com")
    AbsoluteUrl.parse("https://a.com/").removeEmptyPathParts.toString() should equal("https://a.com/")
    AbsoluteUrl.parse("https://a.com//").removeEmptyPathParts.toString() should equal("https://a.com/")
    AbsoluteUrl.parse("https://a.com//a").removeEmptyPathParts.toString() should equal("https://a.com/a")
    AbsoluteUrl.parse("https://a.com//a/").removeEmptyPathParts.toString() should equal("https://a.com/a")
  }
}
