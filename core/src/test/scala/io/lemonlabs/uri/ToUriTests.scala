package io.lemonlabs.uri

import java.net.URI

import io.lemonlabs.uri.config.UriConfig
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class ToUriTests extends AnyWordSpec with Matchers {
  "toJavaURI" should {
    "handle simple URL" in {
      val strUri: String = "http://www.example.com"
      val url = Url.parse(strUri)
      val javaUri: URI = url.toJavaURI
      javaUri.getScheme should equal("http")
      javaUri.getUserInfo should be(null)
      javaUri.getHost should equal("www.example.com")
      javaUri.getPath should equal("")
      javaUri.getQuery should be(null)
      javaUri.getFragment should be(null)
      javaUri.toASCIIString should equal(strUri)
    }

    "handle scheme-less URL" in {
      val strUri: String = "//www.example.com/test"
      val url = Url.parse(strUri)
      val javaUri: URI = url.toJavaURI
      javaUri.getScheme should be(null)
      javaUri.getHost should equal("www.example.com")
      javaUri.getPath should equal("/test")
      javaUri.toASCIIString should equal(strUri)
    }

    "handle authenticated URL" in {
      val strUri: String = "https://user:password@www.example.com/test"
      val url = Url.parse(strUri)
      val javaUri: URI = url.toJavaURI
      javaUri.getScheme should equal("https")
      javaUri.getUserInfo should equal("user:password")
      javaUri.getHost should equal("www.example.com")
      javaUri.getPath should equal("/test")
      javaUri.toASCIIString should equal(strUri)
    }

    "handle exotic/reserved characters in query string" in {
      val url = Url(
        scheme = "http",
        host = "www.example.com",
        path = "/test",
        query = QueryString.fromPairs("weird=&key" -> Some("strange%value"), "arrow" -> Some("⇔"))
      )
      val javaUri: URI = url.toJavaURI
      // javaUri.getScheme should equal("http")
      // javaUri.getHost should equal("www.example.com")
      // javaUri.getPath should equal("/test")
      println(url.toString)
      javaUri.getQuery should equal("weird=&key=strange%value&arrow=⇔")
      // javaUri.getRawQuery should equal("weird%3D%26key=strange%25value&arrow=%E2%87%94")
      // javaUri.toString should equal(url.toString)
      // javaUri.toASCIIString should equal(url.toString)
    }
  }

  "apply" should {
    "handle exotic/reserved characters in query string" in {
      val javaUri: URI =
        new URI("http://user:password@www.example.com/test?weird%3D%26key=strange%25value&arrow=%E2%87%94")
      val url = Uri(javaUri).toUrl
      url.schemeOption should equal(Some("http"))
      url.hostOption should equal(Some(DomainName("www.example.com")))
      url.user should equal(Some("user"))
      url.password should equal(Some("password"))
      url.path.toString() should equal("/test")
      url.query.params should equal(Vector(("weird=&key", Some("strange%value")), ("arrow", Some("⇔"))))
      url.toStringWithConfig(UriConfig.conservative) should equal(javaUri.toASCIIString)
    }
  }
}
