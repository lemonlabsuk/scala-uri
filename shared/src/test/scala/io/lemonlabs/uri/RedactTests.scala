package io.lemonlabs.uri

import io.lemonlabs.uri.redact.Redact
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class RedactTests extends AnyWordSpec with Matchers {
  "Redacting byRemoving" should {
    "remove all parameters" in {
      val url = Url.parse("http://user:password@example.com?secret=123&other=true")
      url.toRedactedString(Redact.byRemoving.allParams()) should equal("http://user:password@example.com")
    }
    "remove parameters by name" in {
      val url = Url.parse("http://user:password@example.com?secret=123&other=true&other=false&last=yes")
      url.toRedactedString(Redact.byRemoving.params("secret", "other")) should equal(
        "http://user:password@example.com?last=yes"
      )
    }
    "remove user info" in {
      val url = Url.parse("http://user:password@example.com?secret=123&other=true")
      url.toRedactedString(Redact.byRemoving.userInfo()) should equal("http://example.com?secret=123&other=true")
    }
    "remove password" in {
      val url = Url.parse("http://user:password@example.com?secret=123&other=true")
      url.toRedactedString(Redact.byRemoving.password()) should equal("http://user@example.com?secret=123&other=true")
    }
    "remove everything" in {
      val url = Url.parse("http://user:password@example.com?secret=123&other=true")
      url.toRedactedString(Redact.byRemoving.allParams().userInfo()) should equal("http://example.com")
    }
  }
  "Redacting withPlaceholder" should {
    "replace all parameters" in {
      val url = Url.parse("http://user:password@example.com?secret=123&other=true")
      url.toRedactedString(Redact.withPlaceholder("xxx").allParams()) should equal(
        "http://user:password@example.com?secret=xxx&other=xxx"
      )
    }
    "replace parameters by name" in {
      val url = Url.parse("http://user:password@example.com?secret=123&other=true&other=false&last=yes")
      url.toRedactedString(Redact.withPlaceholder("xxx").params("secret", "other")) should equal(
        "http://user:password@example.com?secret=xxx&other=xxx&other=xxx&last=yes"
      )
    }
    "replace user" in {
      val url = Url.parse("http://user:password@example.com?secret=123&other=true")
      url.toRedactedString(Redact.withPlaceholder("xxx").user()) should equal(
        "http://xxx:password@example.com?secret=123&other=true"
      )
    }
    "replace password" in {
      val url = Url.parse("http://user:password@example.com?secret=123&other=true")
      url.toRedactedString(Redact.withPlaceholder("xxx").password()) should equal(
        "http://user:xxx@example.com?secret=123&other=true"
      )
    }
    "replace everything" in {
      val url = Url.parse("http://user:password@example.com?secret=123&other=true")
      url.toRedactedString(Redact.withPlaceholder("xxx").allParams().user().password()) should equal(
        "http://xxx:xxx@example.com?secret=xxx&other=xxx"
      )
    }
  }
}
