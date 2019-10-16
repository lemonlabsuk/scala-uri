package io.lemonlabs.uri.json

import io.lemonlabs.uri.NotImplementedForScalaJsError
import io.lemonlabs.uri.inet.Trie

case object ScalaJsJsonSupport extends JsonSupport {
  override def publicSuffixTrie: Trie =
    throw NotImplementedForScalaJsError
}
