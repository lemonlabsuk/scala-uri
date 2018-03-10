package io.lemonlabs.uri

import org.scalatest.{FlatSpec, Matchers}

class PatternMatchingTests extends FlatSpec with Matchers {

  "Uri.unapply" should "extract the path" in {
    val Uri(path) = Urn.parse("urn:nid:nss")
    path should equal(UrnPath("nid", "nss"))

    val Uri(path2) = Url.parse("http://localhost/my/lovely/path")
    path2 should equal(AbsolutePath.fromParts("my", "lovely", "path"))
  }

  "Url.unapply" should "extract the path, query and fragment" in {
    val Url(path, query, fragment) = Url.parse("/test?query=string#frag")
    path should equal(AbsolutePath.fromParts("test"))
    query should equal(QueryString.fromPairs("query" -> "string"))
    fragment should equal(Some("frag"))
  }

  "RelativeUrl.unapply" should "extract the path, query and fragment" in {
    val RelativeUrl(path, query, fragment)= Url.parse("/test?query=string#frag")
    path should equal(AbsolutePath.fromParts("test"))
    query should equal(QueryString.fromPairs("query" -> "string"))
    fragment should equal(Some("frag"))
  }

  "UrlWithAuthority.unapply" should "extract the authority, path, query and fragment" in {
    val UrlWithAuthority(authority, path, query, fragment) =
      Url.parse("http://localhost/test?query=string#frag")

    authority.host should equal(DomainName("localhost"))
    path should equal(AbsolutePath.fromParts("test"))
    query should equal(QueryString.fromPairs("query" -> "string"))
    fragment should equal(Some("frag"))
  }

  "Host.unapply" should "extract the authority, path, query and fragment" in {
    val Host(domain) = DomainName("abc")
    domain should equal("abc")

    val Host(ipv4) = IpV4.parse("1.2.3.4")
    ipv4 should equal("1.2.3.4")

    val Host(ipv6) = IpV6.parse("[1:2:3:4:5:6:7:8]")
    ipv6 should equal("[1:2:3:4:5:6:7:8]")
  }

  "PathParts.unapply()" should "match empty path and single slash only" in {
    // Should all run without throwing exception
    val PathParts() = RootlessPath(Vector.empty)
    val PathParts() = AbsolutePath(Vector.empty)
    val PathParts() = EmptyPath

    // Should not match
    a[MatchError] should be thrownBy { val PathParts() = RootlessPath.fromParts("a") }
    a[MatchError] should be thrownBy { val PathParts() = AbsolutePath.fromParts("a") }
  }

  "PathParts.unapply(\"a\", \"b\")" should "match '/a/b' and 'a/b' only" in {
    // Should all run without throwing exception
    val PathParts("a", "b") = RootlessPath.fromParts("a", "b")
    val PathParts("a", "b") = AbsolutePath.fromParts("a", "b")

    // Should not match
    a[MatchError] should be thrownBy { val PathParts("a", "b") = RootlessPath.fromParts("a") }
    a[MatchError] should be thrownBy { val PathParts("a", "b") = AbsolutePath.fromParts("a") }
  }

  "PathParts.unapply(\"a\", _*)" should "match '/a.*' and 'a.*' only" in {
    // Should all run without throwing exception
    val PathParts("a", _*) = RootlessPath.fromParts("a")
    val PathParts("a", _*) = AbsolutePath.fromParts("a")
    val PathParts("a", _*) = RootlessPath.fromParts("a", "b")
    val PathParts("a", _*) = AbsolutePath.fromParts("a", "b")

    // Should not match
    a[MatchError] should be thrownBy { val PathParts("a", _*) = RootlessPath.fromParts("x", "y", "z") }
    a[MatchError] should be thrownBy { val PathParts("a", _*) = AbsolutePath.fromParts("x", "y", "z") }
  }

  "EmptyPath.unapply()" should "match empty paths only, but not single slash" in {
    // Should all run without throwing exception
    val EmptyPath() = EmptyPath
    val EmptyPath() = RootlessPath(Vector.empty)

    // Should not match
    a[MatchError] should be thrownBy { val EmptyPath() = AbsolutePath(Vector.empty) }
  }

  "Path.unapply()" should "match any path" in {
    val Path(parts) = AbsolutePath.fromParts("a", "b", "c")
    parts should equal(Vector("a", "b", "c"))
  }
}
