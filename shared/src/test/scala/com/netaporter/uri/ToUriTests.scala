package com.netaporter.uri

import org.scalatest.WordSpec
import org.scalatest.Matchers
import dsl._
import java.net.URI
import com.netaporter.uri.config.UriConfig

class ToUriTests extends WordSpec with Matchers {
  "toUri" should {
    "handle simple URL" in {
      val strUri: String = "http://www.example.com"
      val uri: Uri = strUri
      val javaUri: URI = uri.toURI
      javaUri.getScheme() should equal("http")
      javaUri.getUserInfo() should be(null)
      javaUri.getHost() should equal("www.example.com")
      javaUri.getPath() should equal("")
      javaUri.getQuery() should be(null)
      javaUri.getFragment() should be(null)
      javaUri.toASCIIString() should equal(strUri)
    }

    "handle scheme-less URL" in {
      val strUri: String = "//www.example.com/test"
      val uri: Uri = strUri
      val javaUri: URI = uri.toURI
      javaUri.getScheme() should be(null)
      javaUri.getHost() should equal("www.example.com")
      javaUri.getPath() should equal("/test")
      javaUri.toASCIIString() should equal(strUri)
    }

    "handle authenticated URL" in {
      val strUri: String = "https://user:password@www.example.com/test"
      val uri: Uri = strUri
      val javaUri: URI = uri.toURI
      javaUri.getScheme() should equal("https")
      javaUri.getUserInfo() should equal("user:password")
      javaUri.getHost() should equal("www.example.com")
      javaUri.getPath() should equal("/test")
      javaUri.toASCIIString() should equal(strUri)
    }

    "handle exotic/reserved characters in query string" in {
      val uri: Uri = "http://www.example.com/test" ? ("weird=&key" -> "strange%value") & ("arrow" -> "⇔")
      val javaUri: URI = uri.toURI
      javaUri.getScheme() should equal("http")
      javaUri.getHost() should equal("www.example.com")
      javaUri.getPath() should equal("/test")
      javaUri.getQuery() should equal("weird=&key=strange%value&arrow=⇔")
      javaUri.getRawQuery() should equal("weird%3D%26key=strange%25value&arrow=%E2%87%94")
      javaUri.toString() should equal(uri.toString)
      javaUri.toASCIIString() should equal(uri.toString)
    }
  }
  
  "apply" should {

    "handle exotic/reserved characters in query string" in {
      val javaUri: URI = new URI("http://user:password@www.example.com/test?weird%3D%26key=strange%25value&arrow=%E2%87%94")
      val uri: Uri = Uri(javaUri)
      uri.scheme should equal(Some("http"))
      uri.host should equal(Some("www.example.com"))
      uri.user should equal(Some("user"))
      uri.password should equal(Some("password"))
      uri.path should equal("/test")
      uri.query.params should equal(Seq(("weird=&key", Some("strange%value")), ("arrow", Some("⇔"))))
      uri.toString(UriConfig.conservative) should equal(javaUri.toASCIIString())
    }
  }
}