package io.lemonlabs.uri.json

import io.lemonlabs.uri.inet.Trie

import scala.io.Source

trait JsonSupport extends Product with Serializable {
  def publicSuffixTrie: Trie

  protected def publicSuffixJson: String = {
    val source = Source.fromURL(getClass.getResource("/public_suffix_trie.json"), "UTF-8")
    val json = source.mkString
    source.close()
    json
  }
}
