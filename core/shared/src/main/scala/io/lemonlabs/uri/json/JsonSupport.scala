package io.lemonlabs.uri.json

import io.lemonlabs.uri.inet.Trie

import scala.annotation.implicitNotFound
import scala.io.Source

@implicitNotFound(
  "This scala-uri functionality requires a JSON parser\n" +
    "Please ensure you have added the scala-uri-circe or scala-uri-spray-json " +
    "dependency to your SBT build and have the import io.lemonlabs.uri.json._\n" +
    "If you are using Scala.js, unfortunately this functionality is not yet supported"
)
trait JsonSupport extends Product with Serializable {
  def publicSuffixTrie: Trie

  protected def publicSuffixJson: String = {
    val source = Source.fromURL(getClass.getResource("/public_suffix_trie.json"), "UTF-8")
    val json = source.mkString
    source.close()
    json
  }
}
