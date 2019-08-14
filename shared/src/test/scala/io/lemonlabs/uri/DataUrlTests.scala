package io.lemonlabs.uri

import io.lemonlabs.uri.config.UriConfig
import io.lemonlabs.uri.encoding.PercentEncoder
import org.scalatest.{FlatSpec, Matchers}

class DataUrlTests extends FlatSpec with Matchers {

  "Authority, querystring, etc" should "be empty" in {
    val dataUrl = DataUrl.parse("data:,A%20brief%20note")
    dataUrl.port should equal(None)
    dataUrl.user should equal(None)
    dataUrl.password should equal(None)
    dataUrl.publicSuffix should equal(None)
    dataUrl.publicSuffixes should equal(Vector.empty)
    dataUrl.subdomain should equal(None)
    dataUrl.subdomains should equal(Vector.empty)
    dataUrl.shortestSubdomain should equal(None)
    dataUrl.longestSubdomain should equal(None)
  }

  /**
    * From Section 4 Examples in https://tools.ietf.org/html/rfc2397
    */
  "Missing mediatype" should "default to text/plain;charset=US-ASCII" in {
    val dataUrl = DataUrl.parse("data:,A%20brief%20note")
    dataUrl.mediaType.toString should equal("")
    dataUrl.mediaType.rawValue should equal(None)
    dataUrl.mediaType.value should equal("text/plain")
    dataUrl.mediaType.rawCharset should equal(None)
    dataUrl.mediaType.charset should equal("US-ASCII")
  }

  "Mediatype parameters" should "be parsed" in {
    val dataUrl = DataUrl.parse("data:text/plain;charset=UTF-8;page=21,the%20data:1234,5678")
    dataUrl.mediaType.parameters should equal(
      Vector(
        "charset" -> "UTF-8",
        "page" -> "21"
      )
    )
  }

  "A quoted charset" should "be respected" in {
    val dataUrl = DataUrl.parse("data:text/plain;\"Charset\"=UTF-8,the%20data")
    dataUrl.mediaType.charset should equal("UTF-8")
  }

  /**
    * From https://developer.mozilla.org/en-US/docs/Web/HTTP/Basics_of_HTTP/Data_URIs
    */
  "Base64 encoded data" should "be percent decoded" in {
    val dataUrl = DataUrl.parse("data:text/plain;base64,SGVsbG8sIFdvcmxkIQ%3D%3D")
    dataUrl.dataAsString should equal("Hello, World!")
    dataUrl.toString() should equal("data:text/plain;base64,SGVsbG8sIFdvcmxkIQ==")
  }

  it should "have '=' padding chars percent encoded when configured to" in {
    implicit val c = UriConfig(encoder = PercentEncoder() ++ '=')
    val dataUrl = DataUrl.parse("data:text/plain;base64,SGVsbG8sIFdvcmxkIQ%3D%3D")
    dataUrl.dataAsString should equal("Hello, World!")
    dataUrl.toString() should equal("data:text/plain;base64,SGVsbG8sIFdvcmxkIQ%3D%3D")
  }

  /**
    * From https://developer.mozilla.org/en-US/docs/Web/HTTP/Basics_of_HTTP/Data_URIs
    */
  "Percent Encoded data" should "be decoded and encoded by default" in {
    val dataUrl = DataUrl.parse("data:text/html,%3Ch1%3EHello%2C%20World!%3C%2Fh1%3E")
    dataUrl.dataAsString should equal("<h1>Hello, World!</h1>")
    dataUrl.toString() should equal("data:text/html,%3Ch1%3EHello,%20World!%3C%2Fh1%3E")
  }

  /**
    * From https://developer.mozilla.org/en-US/docs/Web/HTTP/Basics_of_HTTP/Data_URIs
    */
  "Url.parse" should "return a DataUrl" in {
    val dataUrl = Url.parse("data:,Hello%2C%20World!")
    dataUrl shouldBe a[DataUrl]
    dataUrl.schemeOption should equal(Some("data"))
    dataUrl.path.toStringRaw should equal(",Hello, World!")
    dataUrl.path.toString() should equal(",Hello,%20World!")
  }

  /**
    * From https://en.wikipedia.org/wiki/Data_URI_scheme#Syntax
    */
  "MediaType" should "return type, subtype and suffix" in {
    val dataUrl = DataUrl.parse("data:text/vnd-example+xyz;foo=bar;base64,R0lGODdh")
    dataUrl.mediaType.typ should equal("text")
    dataUrl.mediaType.subTyp should equal("vnd-example")
    dataUrl.mediaType.suffix should equal("xyz")
  }

  it should "return empty string for suffix when there is none" in {
    val dataUrl = DataUrl.parse("data:text/vnd-example;foo=bar;base64,R0lGODdh")
    dataUrl.mediaType.typ should equal("text")
    dataUrl.mediaType.subTyp should equal("vnd-example")
    dataUrl.mediaType.suffix should equal("")
  }

  it should "return empty string for subtype and suffix when there is none" in {
    val dataUrl = DataUrl.parse("data:text;base64,R0lGODdh")
    dataUrl.mediaType.typ should equal("text")
    dataUrl.mediaType.subTyp should equal("")
    dataUrl.mediaType.suffix should equal("")
  }

  "Changing scheme" should "return a SimpleUrlWithoutAuthority" in {
    val dataUrl = DataUrl.parse("data:,A%20brief%20note")
    val mailto = dataUrl.withScheme("mailto")
    mailto shouldBe a[SimpleUrlWithoutAuthority]
    mailto.toString() should equal("mailto:,A%20brief%20note")
  }

  it should "return a DataUrl if the scheme is data" in {
    val dataUrl = DataUrl.parse("data:,A%20brief%20note")
    val dataUrl2 = dataUrl.withScheme("data")
    dataUrl2 should be theSameInstanceAs dataUrl
  }

  "Changing fragment" should "return self" in {
    val dataUrl = DataUrl.parse("data:,A%20brief%20note")
    val url = dataUrl.withFragment("fragment")
    url should be theSameInstanceAs dataUrl
  }

  "Changing querystring" should "return self" in {
    val dataUrl = DataUrl.parse("data:,A%20brief%20note")
    val url = dataUrl.withQueryString("a" -> "b")
    url should be theSameInstanceAs dataUrl
  }

  "Changing Host" should "return an AbsoluteUrl" in {
    val dataUrl = DataUrl.parse("data:,A%20brief%20note")
    val absoluteUrl = dataUrl.withHost(DomainName("example.com"))
    absoluteUrl shouldBe an[AbsoluteUrl]
    absoluteUrl.toString() should equal("data://example.com/,A%20brief%20note")
  }

  "Changing Port" should "return an AbsoluteUrl" in {
    val dataUrl = DataUrl.parse("data:,A%20brief%20note")
    val absoluteUrl = dataUrl.withPort(8080)
    absoluteUrl shouldBe an[AbsoluteUrl]
    absoluteUrl.toString() should equal("data://:8080/,A%20brief%20note")
  }

  "Changing Authority" should "return an AbsoluteUrl" in {
    implicit val config: UriConfig = UriConfig.default
    val dataUrl = DataUrl.parse("data:,A%20brief%20note")
    val absoluteUrl = dataUrl.withAuthority(Authority("example.com", 8080))
    absoluteUrl shouldBe an[AbsoluteUrl]
    absoluteUrl.toString() should equal("data://example.com:8080/,A%20brief%20note")
  }
}
