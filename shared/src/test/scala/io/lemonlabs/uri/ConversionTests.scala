package io.lemonlabs.uri

import org.scalatest.{Matchers, WordSpec}

class ConversionTests extends WordSpec with Matchers {

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
  }
}
