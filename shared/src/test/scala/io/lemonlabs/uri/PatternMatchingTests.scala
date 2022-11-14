package io.lemonlabs.uri

import org.scalatest.Inside
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class PatternMatchingTests extends AnyFlatSpec with Matchers with Inside {

  "Uri.unapply" should "extract the path" in {
    inside(Urn.parse("urn:nid:nss")) { case Uri(path) =>
      path should equal(UrnPath("nid", "nss"))
    }

    inside(Url.parse("http://localhost/my/lovely/path")) { case Uri(path2) =>
      path2 should equal(AbsolutePath.fromParts("my", "lovely", "path"))
    }
  }

  "Url.unapply" should "extract the path, query and fragment" in {
    inside(Url.parse("/test?query=string#frag")) { case Url(path, query, fragment) =>
      path should equal(AbsolutePath.fromParts("test"))
      query should equal(QueryString.fromPairs("query" -> "string"))
      fragment should equal(Some("frag"))
    }
  }

  "RelativeUrl.unapply" should "extract the path, query and fragment" in {
    inside(Url.parse("/test?query=string#frag")) { case RelativeUrl(path, query, fragment) =>
      path should equal(AbsolutePath.fromParts("test"))
      query should equal(QueryString.fromPairs("query" -> "string"))
      fragment should equal(Some("frag"))
    }
  }

  "UrlWithAuthority.unapply" should "extract the authority, path, query and fragment" in {
    inside(Url.parse("http://localhost/test?query=string#frag")) {
      case UrlWithAuthority(authority, path, query, fragment) =>
        authority.host should equal(DomainName("localhost"))
        path should equal(AbsolutePath.fromParts("test"))
        query should equal(QueryString.fromPairs("query" -> "string"))
        fragment should equal(Some("frag"))
    }
  }

  "Host.unapply" should "extract the authority, path, query and fragment" in {
    inside(DomainName("abc")) { case Host(domain) =>
      domain should equal("abc")
    }

    inside(IpV4.parse("1.2.3.4")) { case Host(ipv4) =>
      ipv4 should equal("1.2.3.4")
    }

    inside(IpV6.parse("[1:2:3:4:5:6:7:8]")) { case Host(ipv6) =>
      ipv6 should equal("[1:2:3:4:5:6:7:8]")
    }
  }

  "PathParts.unapply()" should "match empty path and single slash only" in {
    // Should all run without throwing exception
    RootlessPath(Vector.empty) should matchPattern { case PathParts() => }
    AbsolutePath(Vector.empty) should matchPattern { case PathParts() => }
    EmptyPath should matchPattern { case PathParts() => }

    // Should not match
    RootlessPath.fromParts("a") shouldNot matchPattern { case PathParts() => }
    AbsolutePath.fromParts("a") shouldNot matchPattern { case PathParts() => }
  }

  "PathParts.unapply(\"a\", \"b\")" should "match '/a/b' and 'a/b' only" in {
    // Should all run without throwing exception
    RootlessPath.fromParts("a", "b") should matchPattern { case PathParts("a", "b") => }
    AbsolutePath.fromParts("a", "b") should matchPattern { case PathParts("a", "b") => }

    // Should not match
    RootlessPath.fromParts("a") shouldNot matchPattern { case PathParts("a", "b") => }
    AbsolutePath.fromParts("a") shouldNot matchPattern { case PathParts("a", "b") => }
  }

  "PathParts.unapply(\"a\", _*)" should "match '/a.*' and 'a.*' only" in {
    // Should all run without throwing exception
    RootlessPath.fromParts("a") should matchPattern { case PathParts("a", _*) => }
    AbsolutePath.fromParts("a") should matchPattern { case PathParts("a", _*) => }
    RootlessPath.fromParts("a", "b") should matchPattern { case PathParts("a", _*) => }
    AbsolutePath.fromParts("a", "b") should matchPattern { case PathParts("a", _*) => }

    // Should not match
    RootlessPath.fromParts("x", "y", "z") shouldNot matchPattern { case PathParts("a", _*) => }
    AbsolutePath.fromParts("x", "y", "z") shouldNot matchPattern { case PathParts("a", _*) => }
  }

  "EmptyPath.unapply()" should "match empty paths only, but not single slash" in {
    // Should all run without throwing exception
    EmptyPath should matchPattern { case EmptyPath() => }
    RootlessPath(Vector.empty) should matchPattern { case EmptyPath() => }

    // Should not match
    AbsolutePath(Vector.empty) shouldNot matchPattern { case EmptyPath() => }
  }

  "Path.unapply()" should "match any path" in {
    inside(AbsolutePath.fromParts("a", "b", "c")) { case Path(parts) =>
      parts should equal(Vector("a", "b", "c"))
    }
  }
}
