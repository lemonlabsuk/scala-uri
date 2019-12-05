package io.lemonlabs.uri

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class ApexDomainTests extends AnyFlatSpec with Matchers {
  "IPv4" should "not return apex domain" in {
    Url.parse("http://1.2.3.4").apexDomain should equal(None)
  }

  "IPv6" should "not return apex domain" in {
    Url.parse("http://[::1]").apexDomain should equal(None)
  }

  "DomainName" should "return apex domain" in {
    Url.parse("http://maps.google.com").apexDomain should equal(Some("google.com"))
  }

  it should "return itself for apexDomain when it is the apex domain" in {
    Url.parse("http://google.com").apexDomain should equal(Some("google.com"))
  }

  it should "not return a apexDomain when there is no known public suffix" in {
    Url.parse("http://google.blahblahblah").apexDomain should equal(None)
  }
}
