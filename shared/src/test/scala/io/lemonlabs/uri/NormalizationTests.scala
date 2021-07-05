package io.lemonlabs.uri

import io.lemonlabs.uri.Path.SlashTermination
import io.lemonlabs.uri.config.UriConfig
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class NormalizationTests extends AnyFlatSpec with Matchers {
  implicit val config: UriConfig = UriConfig.default

  "Host" should "normalize correctly" in {
    IpV4.parse("192.168.0.1").normalize should equal(IpV4.parse("192.168.0.1"))
    IpV6.parse("[2001:0db8:85a3:0000:0000:8a2e:0370:7334]") should equal(
      IpV6.parse("[2001:0db8:85a3:0000:0000:8a2e:0370:7334]")
    )
    DomainName("Foo.bar.com").normalize should equal(DomainName("foo.bar.com"))
  }

  "Authority" should "normalize correctly" in {
    Authority(DomainName("Foo.bar.com"), 80).normalize(None) should equal(
      Authority(DomainName("foo.bar.com"), 80)
    )
    Authority(DomainName("Foo.bar.com"), 80).normalize(Some("http")) should equal(
      Authority(DomainName("foo.bar.com"))
    )
  }

  "UrlPath.normalize" should "remove dot parts" in {
    UrlPath(Seq("")).normalize(false) should equal(UrlPath.slash)
    UrlPath(Seq("A")).normalize(false) should equal(AbsolutePath.fromParts("A"))
    UrlPath(Seq(".")).normalize(false) should equal(UrlPath.slash)
    UrlPath(Seq("..")).normalize(false) should equal(UrlPath.slash)
    UrlPath(Seq("a", ".", "b")).normalize(false) should equal(AbsolutePath.fromParts("a", "b"))
    UrlPath(Seq("a", "..", "b")).normalize(false) should equal(AbsolutePath.fromParts("b"))
  }

  it should "remove empty parts" in {
    UrlPath(Seq("")).normalize(true) should equal(UrlPath.slash)
    UrlPath(Seq("a", "", "b")).normalize(true) should equal(AbsolutePath.fromParts("a", "b"))
    UrlPath(Seq("a", "", "b")).normalize(true) should equal(AbsolutePath.fromParts("a", "b"))
    UrlPath(Seq("a")).normalize(true, slashTermination = SlashTermination.AddForAll) should equal(
      AbsolutePath.fromParts("a", "")
    )
  }

  it should "apply slash termination policy" in {

    RootlessPath.fromParts("").normalize(slashTermination = SlashTermination.Off) should equal(
      RootlessPath.fromParts("")
    )
    RootlessPath.fromParts("").normalize(slashTermination = SlashTermination.RemoveForAll) should equal(EmptyPath)
    RootlessPath.fromParts("").normalize(slashTermination = SlashTermination.AddForEmptyPath) should equal(
      UrlPath.slash
    )
    RootlessPath.fromParts("").normalize(slashTermination = SlashTermination.AddForEmptyPathRemoveOthers) should equal(
      UrlPath.slash
    )
    RootlessPath.fromParts("").normalize(slashTermination = SlashTermination.AddForAll) should equal(UrlPath.slash)

    AbsolutePath.fromParts("").normalize(slashTermination = SlashTermination.Off) should equal(
      AbsolutePath.fromParts("")
    )
    AbsolutePath.fromParts("").normalize(slashTermination = SlashTermination.RemoveForAll) should equal(EmptyPath)
    AbsolutePath.fromParts("").normalize(slashTermination = SlashTermination.AddForEmptyPath) should equal(
      UrlPath.slash
    )
    AbsolutePath.fromParts("").normalize(slashTermination = SlashTermination.AddForEmptyPathRemoveOthers) should equal(
      UrlPath.slash
    )
    AbsolutePath.fromParts("").normalize(slashTermination = SlashTermination.AddForAll) should equal(UrlPath.slash)

    RootlessPath.fromParts("a").normalize(slashTermination = SlashTermination.Off) should
      equal(RootlessPath.fromParts("a"))
    RootlessPath.fromParts("a").normalize(slashTermination = SlashTermination.RemoveForAll) should
      equal(RootlessPath.fromParts("a"))
    RootlessPath.fromParts("a").normalize(slashTermination = SlashTermination.AddForEmptyPath) should
      equal(RootlessPath.fromParts("a"))
    RootlessPath.fromParts("a").normalize(slashTermination = SlashTermination.AddForEmptyPathRemoveOthers) should
      equal(RootlessPath.fromParts("a"))
    RootlessPath.fromParts("a").normalize(slashTermination = SlashTermination.AddForAll) should
      equal(RootlessPath.fromParts("a", ""))

    AbsolutePath.fromParts("a").normalize(slashTermination = SlashTermination.Off) should
      equal(AbsolutePath.fromParts("a"))
    AbsolutePath.fromParts("a").normalize(slashTermination = SlashTermination.RemoveForAll) should
      equal(AbsolutePath.fromParts("a"))
    AbsolutePath.fromParts("a").normalize(slashTermination = SlashTermination.AddForEmptyPath) should
      equal(AbsolutePath.fromParts("a"))
    AbsolutePath.fromParts("a").normalize(slashTermination = SlashTermination.AddForEmptyPathRemoveOthers) should
      equal(AbsolutePath.fromParts("a"))
    AbsolutePath.fromParts("a").normalize(slashTermination = SlashTermination.AddForAll) should
      equal(AbsolutePath.fromParts("a", ""))
  }

  "SimpleUrlWithoutAuthority" should "normalize the url" in {
    SimpleUrlWithoutAuthority.parse("MAILTO:foo@BAR.com").normalize().toString() should equal("mailto:foo@BAR.com")
  }

  "AbsoluteUrl" should "normalize the url" in {
    AbsoluteUrl.parse("https://FOO.BAR.COM/BAZ/.././BAM").normalize().toString() should equal(
      "https://foo.bar.com/BAM"
    )
    AbsoluteUrl.parse("https://FOO.BAR.COM:443/BAZ/.././BAM").normalize().toString() should equal(
      "https://foo.bar.com/BAM"
    )
    AbsoluteUrl.parse("https://FOO.COM").normalize().toString() should equal("https://foo.com/")
  }

  "ProtocolRelativeUrl" should "normalize the url" in {
    ProtocolRelativeUrl.parse("//FOO.BAR.COM/BAZ/.././BAM").normalize().toString() should equal("//foo.bar.com/BAM")
    ProtocolRelativeUrl.parse("//FOO.BAR.COM:80/BAZ/.././BAM").normalize().toString() should equal(
      "//foo.bar.com:80/BAM"
    )
  }

  "RelativeUrl" should "normalize the url" in {
    RelativeUrl.parse("/FOO/BAR/.././BAM").normalize().toString() should equal("/FOO/BAM")
  }

  "ScpLikeUrl" should "normalize the url" in {
    ScpLikeUrl.parse("something@FOO.BAR.COM:BAZ/.././BAM.git").normalize().toString() should equal(
      "something@foo.bar.com:BAM.git"
    )

  }

  "DataUrl.normalize" should "normalize the url" in {
    DataUrl.parse("data:text/plain;charset=UTF-8;page=21,the%2fdata:1234,5678").toString() should equal(
      "data:text/plain;charset=UTF-8;page=21,the%2Fdata:1234,5678"
    )
  }
}
