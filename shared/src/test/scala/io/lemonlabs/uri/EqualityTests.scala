package io.lemonlabs.uri

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.scalatestplus.scalacheck.ScalaCheckDrivenPropertyChecks

class EqualityTests extends AnyFlatSpec with Matchers with ScalaCheckDrivenPropertyChecks {
  "QueryString.equalsUnordered" should "require the same number of identical parameters" in {
    val qsOne = QueryString.parse("a=1&a=1&a=1&a=1")
    val qsTwo = QueryString.parse("a=1&a=1&a=1")
    qsOne.equalsUnordered(qsTwo) should equal(false)
  }

  it should "require the same number of value-less parameters" in {
    val qsOne = QueryString.parse("a=1&a")
    val qsTwo = QueryString.parse("a=1")
    qsOne.equalsUnordered(qsTwo) should equal(false)
  }

  it should "require the same number of empty parameters" in {
    val qsOne = QueryString.parse("a=1&a=")
    val qsTwo = QueryString.parse("a=1")
    qsOne.equalsUnordered(qsTwo) should equal(false)
  }

  it should "match when in the same order" in {
    val qsOne = QueryString.parse("a=1&b=2&b=2")
    val qsTwo = QueryString.parse("a=1&b=2&b=2")
    qsOne.equalsUnordered(qsTwo) should equal(true)
  }

  it should "match when in a different order" in {
    val qsOne = QueryString.parse("a=1&b=2&b=2")
    val qsTwo = QueryString.parse("b=2&a=1&b=2")
    qsOne.equalsUnordered(qsTwo) should equal(true)
  }

  "Uri.equalsUnordered" should "require the same number of identical parameters" in {
    val urlOne = Url.parse("http://example.com?a=1&a=1&a=1&a=1")
    val urlTwo = Url.parse("http://example.com?a=1&a=1&a=1")
    urlOne.equalsUnordered(urlTwo) should equal(false)
  }

  it should "require the same number of value-less parameters" in {
    val urlOne = Url.parse("http://example.com?a=1&a")
    val urlTwo = Url.parse("http://example.com?a=1")
    urlOne.equalsUnordered(urlTwo) should equal(false)
  }

  it should "require the same number of empty parameters" in {
    val urlOne = Url.parse("http://example.com?a=1&a=")
    val urlTwo = Url.parse("http://example.com?a=1")
    urlOne.equalsUnordered(urlTwo) should equal(false)
  }

  it should "match when in the same order" in {
    val urlOne = Url.parse("http://example.com?a=1&b=2&b=2")
    val urlTwo = Url.parse("http://example.com?a=1&b=2&b=2")
    urlOne.equalsUnordered(urlTwo) should equal(true)
  }

  it should "match when in a different order" in {
    val urlOne = Url.parse("http://example.com?a=1&b=2&b=2")
    val urlTwo = Url.parse("http://example.com?b=2&a=1&b=2")
    urlOne.equalsUnordered(urlTwo) should equal(true)
  }

  it should "not match URLs with URNs" in {
    val urlOne = Url.parse("http://example.com")
    val urnTwo = Urn.parse("urn:example:com")
    urlOne.equalsUnordered(urnTwo) should equal(false)
  }

  it should "not match different types of URL" in {
    val urlOne = Url.parse("http://example.com?a=1&b=2")
    val urlTwo = Url.parse("//example.com?a=1&b=2")
    urlOne.equalsUnordered(urlTwo) should equal(false)
  }

  it should "be the same as == for DataUrls" in {
    val urlOne = DataUrl.parse("data:,A%20brief%20note")
    val urlTwo = Url.parse("data:,A%20brief%20note")
    urlOne.equalsUnordered(urlTwo) should equal(true)
  }

  it should "be the same as == for ScpUrls" in {
    val urlOne = ScpLikeUrl.parse("root@host:/root/file.tar.gz")
    val urlTwo = ScpLikeUrl.parse("root@host:/root/file.tar.gz")
    urlOne.equalsUnordered(urlTwo) should equal(true)
  }

  it should "be the same as == for Urns" in {
    val urnOne = Urn.parse("urn:example:com")
    val urnTwo = Urn.parse("urn:example:com")
    urnOne.equalsUnordered(urnTwo) should equal(true)
  }

  "DataUrl.equals" should "equal itself" in new UriScalaCheckGenerators {
    forAll { (dataUrl: DataUrl) =>
      (dataUrl == dataUrl) should equal(true)
    }
  }

  it should "not equal another DataUrl" in new UriScalaCheckGenerators {
    forAll { (dataUrl: DataUrl, dataUrl2: DataUrl) =>
      (dataUrl == dataUrl2) should equal(false)
    }
  }

  "DataUrl.hashCode" should "equal itself" in new UriScalaCheckGenerators {
    forAll { (dataUrl: DataUrl) =>
      dataUrl.hashCode() should equal(dataUrl.hashCode())
    }
  }

  it should "not equal another DataUrl" in new UriScalaCheckGenerators {
    forAll { (dataUrl: DataUrl, dataUrl2: DataUrl) =>
      dataUrl.hashCode() should not equal dataUrl2.hashCode()
    }
  }
}
