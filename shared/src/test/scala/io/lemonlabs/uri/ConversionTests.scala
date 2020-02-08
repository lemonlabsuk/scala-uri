package io.lemonlabs.uri

import io.lemonlabs.uri.config.UriConfig
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class ConversionTests extends AnyWordSpec with Matchers {
  "AbsoluteUrl" should {
    "convert toUrl with a cast" in {
      val url: Uri = Uri.parse("http://www.example.com/path?q=1#fragment")
      val sameUrl: Url = url.toUrl
      sameUrl should be theSameInstanceAs url
    }

    "not convert toUrn" in {
      val url: Uri = Uri.parse("http://www.example.com/path?q=1#fragment")
      val e = the[UriConversionException] thrownBy url.toUrn
      e.getMessage should equal("AbsoluteUrl cannot be converted to Urn")
    }

    "convert toAbsoluteUrl with a cast" in {
      val url: Url = Url.parse("http://www.example.com/path?q=1#fragment")
      val absUrl: AbsoluteUrl = url.toAbsoluteUrl
      absUrl should be theSameInstanceAs url
    }

    "convert toRelativeUrl" in {
      val url: Url = Url.parse("http://www.example.com/path?q=1#fragment")
      val relUrl = url.toRelativeUrl
      relUrl.toString() should equal("/path?q=1#fragment")
    }

    "convert toProtocolRelativeUrl" in {
      val url: Url = Url.parse("http://www.example.com/path?q=1#fragment")
      val protocolRelUrl = url.toProtocolRelativeUrl
      protocolRelUrl.toString() should equal("//www.example.com/path?q=1#fragment")
    }
  }

  "RelativeUrl" should {
    "convert toUrl with a noop" in {
      val url: Uri = Uri.parse("/path?q=1#fragment")
      val sameUrl: Url = url.toUrl
      sameUrl should be theSameInstanceAs url
    }

    "not convert toUrn" in {
      val url: Uri = Uri.parse("/path?q=1#fragment")
      val e = the[UriConversionException] thrownBy url.toUrn
      e.getMessage should equal("RelativeUrl cannot be converted to Urn")
    }

    "not convert toAbsoluteUrl" in {
      val url: Url = Url.parse("/path?q=1#fragment")
      val e = the[UriConversionException] thrownBy url.toAbsoluteUrl
      e.getMessage should equal("RelativeUrl cannot be converted to AbsoluteUrl")
    }

    "convert toRelativeUrl with a cast" in {
      val url: Url = Url.parse("/path?q=1#fragment")
      val relUrl = url.toRelativeUrl
      relUrl should be theSameInstanceAs url
    }

    "not convert toProtocolRelativeUrl" in {
      val url: Url = Url.parse("/path?q=1#fragment")
      val e = the[UriConversionException] thrownBy url.toProtocolRelativeUrl
      e.getMessage should equal("RelativeUrl cannot be converted to ProtocolRelativeUrl")
    }

    "convert to UrlWithoutAuthority via withScheme" in {
      val url = RelativeUrl.parse("me@example.com")
      val mailto: UrlWithoutAuthority = url.withScheme("mailto")
      mailto.toString() should equal("mailto:me@example.com")
    }

    "convert to ProtocolRelativeUrl via withAuthority" in {
      implicit val c: UriConfig = UriConfig.default
      val url = RelativeUrl.parse("/index.html")
      val url2: ProtocolRelativeUrl = url.withAuthority(Authority("example.com"))
      url2.toString() should equal("//example.com/index.html")
    }

    "convert to ProtocolRelativeUrl via withHost" in {
      val url = RelativeUrl.parse("/index.html")
      val url2: ProtocolRelativeUrl = url.withHost(IpV4(127, 0, 0, 1))
      url2.toString() should equal("//127.0.0.1/index.html")
    }
  }

  "ProtocolRelativeUrl" should {
    "convert toUrl with a noop" in {
      val url: Uri = Uri.parse("//www.example.com/path?q=1#fragment")
      val sameUrl: Url = url.toUrl
      sameUrl should be theSameInstanceAs url
    }

    "not convert toUrn" in {
      val url: Uri = Uri.parse("//www.example.com/path?q=1#fragment")
      val e = the[UriConversionException] thrownBy url.toUrn
      e.getMessage should equal("ProtocolRelativeUrl cannot be converted to Urn")
    }

    "not convert toAbsoluteUrl" in {
      val url: Url = Url.parse("//www.example.com/path?q=1#fragment")
      val e = the[UriConversionException] thrownBy url.toAbsoluteUrl
      e.getMessage should equal("ProtocolRelativeUrl cannot be converted to AbsoluteUrl")
    }

    "convert toRelativeUrl" in {
      val url: Url = Url.parse("//www.example.com/path?q=1#fragment")
      val relUrl = url.toRelativeUrl
      relUrl.toString() should equal("/path?q=1#fragment")
    }

    "convert toProtocolRelativeUrl with a cast" in {
      val url: Url = Url.parse("//www.example.com/path?q=1#fragment")
      val protocolRelUrl: ProtocolRelativeUrl = url.toProtocolRelativeUrl
      protocolRelUrl should be theSameInstanceAs url
    }

    "convert to AbsoluteUrl via withScheme" in {
      val url = ProtocolRelativeUrl.parse("//example.com")
      val https: AbsoluteUrl = url.withScheme("https")
      https.toString() should equal("https://example.com")
    }

    "stay as ProtocolRelativeUrl via withAuthority" in {
      implicit val c: UriConfig = UriConfig.default
      val url = ProtocolRelativeUrl.parse("//example.com/index.html")
      val url2: ProtocolRelativeUrl = url.withAuthority(Authority("google.com"))
      url2.toString() should equal("//google.com/index.html")
    }

    "stay as ProtocolRelativeUrl via withFragment" in {
      val url = ProtocolRelativeUrl.parse("//example.com/index.html")
      val url2: ProtocolRelativeUrl = url.withFragment("frag")
      url2.toString() should equal("//example.com/index.html#frag")
    }

    "stay as ProtocolRelativeUrl via withPath" in {
      val url = ProtocolRelativeUrl.parse("//example.com/index.html")
      val url2: ProtocolRelativeUrl = url.withPath(UrlPath.parse("/path"))
      url2.toString() should equal("//example.com/path")
    }

    "stay as ProtocolRelativeUrl via withQueryString" in {
      val url = ProtocolRelativeUrl.parse("//example.com/index.html")
      val url2: ProtocolRelativeUrl = url.withQueryString(QueryString.fromPairs("a" -> "b"))
      url2.toString() should equal("//example.com/index.html?a=b")
    }
  }

  "UrlWithoutAuthority" should {
    "convert toUrl with a noop" in {
      val url: Uri = Uri.parse("mailto:example@example.com")
      val sameUrl: Url = url.toUrl
      sameUrl should be theSameInstanceAs url
    }

    "not convert toUrn" in {
      val url: Uri = Uri.parse("mailto:example@example.com")
      val e = the[UriConversionException] thrownBy url.toUrn
      e.getMessage should equal("SimpleUrlWithoutAuthority cannot be converted to Urn")
    }

    "not convert toAbsoluteUrl" in {
      val url: Url = Url.parse("mailto:example@example.com")
      val e = the[UriConversionException] thrownBy url.toAbsoluteUrl
      e.getMessage should equal("SimpleUrlWithoutAuthority cannot be converted to AbsoluteUrl")
    }

    "convert toRelativeUrl" in {
      // RFC-3986, Section 3.3 - the URI <mailto:fred@example.com> has a path of "fred@example.com"
      val url: Url = Url.parse("mailto:example@example.com")
      val relUrl = url.toRelativeUrl
      relUrl.toString() should equal("example@example.com")
    }

    "not convert toProtocolRelativeUrl" in {
      val url: Url = Url.parse("mailto:example@example.com")
      val e = the[UriConversionException] thrownBy url.toProtocolRelativeUrl
      e.getMessage should equal("SimpleUrlWithoutAuthority cannot be converted to ProtocolRelativeUrl")
    }

    "stay as SimpleUrlWithoutAuthority via withScheme" in {
      val url = SimpleUrlWithoutAuthority.parse("mailto:me@example.com")
      val url2: SimpleUrlWithoutAuthority = url.withScheme("tel")
      url2.toString() should equal("tel:me@example.com")
    }

    "convert to AbsoluteUrl via withAuthority" in {
      implicit val c: UriConfig = UriConfig.default
      val url = SimpleUrlWithoutAuthority.parse("mailto:me@example.com")
      val url2: AbsoluteUrl = url.withAuthority(Authority("google.com"))
      url2.toString() should equal("mailto://google.com/me@example.com")
    }

    "convert to AbsoluteUrl via withHost" in {
      val url = SimpleUrlWithoutAuthority.parse("mailto:me@example.com")
      val url2: AbsoluteUrl = url.withHost(DomainName("google.com"))
      url2.toString() should equal("mailto://google.com/me@example.com")
    }

    "convert to AbsoluteUrl via withPort" in {
      val url = SimpleUrlWithoutAuthority.parse("mailto:me@example.com")
      val url2: AbsoluteUrl = url.withPort(8080)
      url2.toString() should equal("mailto://:8080/me@example.com")
    }

    "stay as SimpleUrlWithoutAuthority via withFragment" in {
      val url = SimpleUrlWithoutAuthority.parse("mailto:me@example.com")
      val url2: SimpleUrlWithoutAuthority = url.withFragment("frag")
      url2.toString() should equal("mailto:me@example.com#frag")
    }

    "stay as SimpleUrlWithoutAuthority via withPath" in {
      val url = SimpleUrlWithoutAuthority.parse("mailto:me@example.com")
      val url2: SimpleUrlWithoutAuthority = url.withPath(UrlPath.parse("someoneelse@example.com"))
      url2.toString() should equal("mailto:someoneelse@example.com")
    }

    "stay as SimpleUrlWithoutAuthority via withQueryString" in {
      val url = SimpleUrlWithoutAuthority.parse("mailto:me@example.com")
      val url2: SimpleUrlWithoutAuthority = url.withQueryString(QueryString.fromPairs("a" -> "b"))
      url2.toString() should equal("mailto:me@example.com?a=b")
    }

    "stay as DataUrl via withPath" in {
      val dataUrl = DataUrl.parse("data:,A%20brief%20note")
      val dataUrl2: DataUrl = dataUrl.withPath(UrlPath.parse(",A%20different%20note"))
      dataUrl2.toString() should equal("data:,A%20different%20note")
    }
  }

  "Urn" should {
    "not convert toUrl" in {
      val urn: Uri = Uri.parse("urn:example:com")

      val e = the[UriConversionException] thrownBy urn.toUrl
      e.getMessage should equal("Urn cannot be converted to Url")
    }

    "convert toUrn with a cast" in {
      val urn: Uri = Uri.parse("urn:example:com")
      val sameUrn: Urn = urn.toUrn
      sameUrn should be theSameInstanceAs urn
    }

    "convert to SimpleUrlWithoutAuthority via withScheme" in {
      val urn: Urn = Urn.parse("urn:44:12345")
      val url: UrlWithoutAuthority = urn.withScheme("tel")
      url.toString() should equal("tel:44/12345")
    }
  }
}
