package io.lemonlabs.uri

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class ScpLikeUrlTests extends AnyFlatSpec with Matchers {
  "ScpLikeUrl.parse" should "parse a git URL" in {
    val uri = ScpLikeUrl.parse("git@github.com:lemonlabsuk/scala-uri.git")
    uri.user should equal(Some("git"))
    uri.host should equal(DomainName("github.com"))
    uri.path should equal(RootlessPath.fromParts("lemonlabsuk", "scala-uri.git"))
    uri.toString() should equal("git@github.com:lemonlabsuk/scala-uri.git")
  }

  it should "parse a URL with absolute path" in {
    val uri = ScpLikeUrl.parse("git@github.com:/lemonlabsuk/scala-uri.git")
    uri.user should equal(Some("git"))
    uri.host should equal(DomainName("github.com"))
    uri.path should equal(AbsolutePath.fromParts("lemonlabsuk", "scala-uri.git"))
    uri.toString() should equal("git@github.com:/lemonlabsuk/scala-uri.git")
  }

  it should "parse a URL without user" in {
    val uri = ScpLikeUrl.parse("github.com:lemonlabsuk/scala-uri.git")
    uri.user should equal(None)
    uri.host should equal(DomainName("github.com"))
    uri.path should equal(RootlessPath.fromParts("lemonlabsuk", "scala-uri.git"))
    uri.toString() should equal("github.com:lemonlabsuk/scala-uri.git")
  }

  it should "parse a URL with IPv4 address" in {
    val uri = ScpLikeUrl.parse("me@127.0.0.1:lemonlabsuk/scala-uri.git")
    uri.user should equal(Some("me"))
    uri.host should equal(IpV4.localhost)
    uri.path should equal(RootlessPath.fromParts("lemonlabsuk", "scala-uri.git"))
    uri.toString() should equal("me@127.0.0.1:lemonlabsuk/scala-uri.git")
  }

  it should "parse a URL with IPv6 address" in {
    val uri = ScpLikeUrl.parse("me@[::1]:lemonlabsuk/scala-uri.git")
    uri.user should equal(Some("me"))
    uri.host should equal(IpV6.localhost)
    uri.path should equal(RootlessPath.fromParts("lemonlabsuk", "scala-uri.git"))
    uri.toString() should equal("me@[::1]:lemonlabsuk/scala-uri.git")
  }

  "ScpLikeUrls" should "support public suffixes" in {
    val uri = ScpLikeUrl.parse("user@host.co.uk:/path")
    uri.publicSuffixes should equal(Vector("co.uk", "uk"))
    uri.publicSuffix should equal(Some("co.uk"))
  }

  it should "support subdomains" in {
    val uri = ScpLikeUrl.parse("user@my.long.host.co.uk:path")
    uri.subdomains should equal(Vector("my", "my.long", "my.long.host"))
    uri.shortestSubdomain should equal(Some("my"))
    uri.longestSubdomain should equal(Some("my.long.host"))
    uri.subdomain should equal(Some("my.long"))
    uri.apexDomain should equal(Some("host.co.uk"))
  }

  it should "convert to AbsoluteUrl via withScheme" in {
    val uri = ScpLikeUrl.parse("user@my.long.host.co.uk:path")
    val httpUri = uri.withScheme("http")
    httpUri.user should equal(Some("user"))
    httpUri.host should equal(DomainName("my.long.host.co.uk"))
    httpUri.path should equal(AbsolutePath.fromParts("path"))
    httpUri.toString() should equal("http://user@my.long.host.co.uk/path")
  }

  it should "not have a scheme, port, password, querystring or fragment" in {
    val uri = ScpLikeUrl.parse("user@my.long.host.co.uk:path")
    uri.schemeOption should equal(None)
    uri.port should equal(None)
    uri.password should equal(None)
    uri.query should equal(QueryString.empty)
    uri.fragment should equal(None)
  }
}
