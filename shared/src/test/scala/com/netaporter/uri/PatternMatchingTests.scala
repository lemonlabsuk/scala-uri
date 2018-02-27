package com.netaporter.uri

import org.scalatest.{FlatSpec, Matchers}

class PatternMatchingTests extends FlatSpec with Matchers {

  "Uri.unapply" should "extract the path" in {
    Urn.parse("urn:nid:nss") match {
      case Uri(path) => path should equal(UrnPath("nid", "nss"))
      case _ => fail("Uri.unapply did not match")
    }

    Url.parse("http://localhost/my/lovely/path") match {
      case Uri(path) => path should equal(AbsolutePath.fromParts("my", "lovely", "path"))
      case _ => fail("Uri.unapply did not match")
    }
  }

  "Url.unapply" should "extract the path, query and fragment" in {
    Url.parse("/test?query=string#frag") match {
      case Url(path, query, fragment) =>
        path should equal(AbsolutePath.fromParts("test"))
        query should equal(QueryString.fromPairs("query" -> "string"))
        fragment should equal(Some("frag"))
      case _ => fail("Url.unapply did not match")
    }
  }

  "UrlWithAuthority.unapply" should "extract the authority, path, query and fragment" in {
    Url.parse("http://localhost/test?query=string#frag") match {
      case UrlWithAuthority(authority, path, query, fragment) =>
        authority.host should equal(DomainName("localhost"))
        path should equal(AbsolutePath.fromParts("test"))
        query should equal(QueryString.fromPairs("query" -> "string"))
        fragment should equal(Some("frag"))
      case _ => fail("UrlWithAuthority.unapply did not match")
    }
  }
}
