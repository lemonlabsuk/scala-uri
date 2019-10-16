package io.lemonlabs.uri

import io.lemonlabs.uri.config.UriConfig
import org.scalatest.{FlatSpec, Matchers}

class ConfigTests extends FlatSpec with Matchers {

  "JsonSupport" should "not be supported for ScalaJS" in {
    a[NotImplementedError] should be thrownBy UriConfig.default.jsonSupport.publicSuffixTrie
  }
}
