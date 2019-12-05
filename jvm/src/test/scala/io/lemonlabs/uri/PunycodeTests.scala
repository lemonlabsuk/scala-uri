package io.lemonlabs.uri

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class PunycodeTests extends AnyFlatSpec with Matchers {
  "Github Issue #26" should "support punycode encoded toString for absolute URLs" in {
    val url = Url.parse("https://はじめよう.みんな/howto.html")
    url.toString should equal("https://はじめよう.みんな/howto.html")
    url.toStringPunycode should equal("https://xn--p8j9a0d9c9a.xn--q9jyb4c/howto.html")
  }

  "Github Issue #26" should "support punycode encoded toString for protocol relative URLs" in {
    val url = Url.parse("//はじめよう.みんな/howto.html")
    url.toString should equal("//はじめよう.みんな/howto.html")
    url.toStringPunycode should equal("//xn--p8j9a0d9c9a.xn--q9jyb4c/howto.html")
  }

  "Github Issue #26" should "leave IPv4 as-is when punycode encoded" in {
    val ipv4 = Url.parse("https://127.0.0.1/howto.html")
    ipv4.toStringPunycode should equal("https://127.0.0.1/howto.html")
  }

  "Github Issue #26" should "leave IPv6 as-is when punycode encoded" in {
    val ipv4 = Url.parse("https://[1f4:0:0:1e::1]/howto.html")
    ipv4.toStringPunycode should equal("https://[1f4:0:0:1e::1]/howto.html")
  }

  "Github Issue #26" should "leave relative URLs as-is when punycode encoded" in {
    val ipv4 = Url.parse("/howto.html")
    ipv4.toStringPunycode should equal("/howto.html")
  }
}
