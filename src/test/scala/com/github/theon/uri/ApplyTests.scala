package com.github.theon.uri

import org.scalatest.FlatSpec
import org.scalatest.matchers.ShouldMatchers

/**
 * Date: 13/04/2013
 * Time: 15:58
 */
class ApplyTests extends FlatSpec with ShouldMatchers {

  "Uri apply method" should "accept String scheme, String host and String path" in {
    val uri = Uri("http", "theon.github.com", "blah")
    uri.protocol should equal(Some("http"))
    uri.host should equal(Some("theon.github.com"))
    uri.path should equal("/blah")
    uri.query should equal(Querystring())
  }

  "Uri apply method" should "accept String scheme, String host, String path and QueryString" in {
    val qs = Querystring(Map("testKey" -> List("testVal")))
    val uri = Uri("http", "theon.github.com", "blah", qs)
    uri.protocol should equal(Some("http"))
    uri.host should equal(Some("theon.github.com"))
    uri.path should equal("/blah")
    uri.query should equal(qs)
  }

  "Uri apply method" should "accept Option[String] scheme, String host, String path and QueryString" in {
    val qs = Querystring(Map("testKey" -> List("testVal")))
    val uri = Uri(Some("http"), "theon.github.com", "blah", qs)
    uri.protocol should equal(Some("http"))
    uri.host should equal(Some("theon.github.com"))
    uri.path should equal("/blah")
    uri.query should equal(qs)
  }

  "Uri apply method" should "accept String path" in {
    val uri = Uri("/blah")
    uri.protocol should equal(None)
    uri.host should equal(None)
    uri.path should equal("/blah")
    uri.query should equal(Querystring())
  }

  "Uri apply method" should "accept String path and QueryString" in {
    val qs = Querystring(Map("testKey" -> List("testVal")))
    val uri = Uri("blah", qs)
    uri.protocol should equal(None)
    uri.host should equal(None)
    uri.path should equal("/blah")
    uri.query should equal(qs)
  }
}
