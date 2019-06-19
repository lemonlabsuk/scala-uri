package io.lemonlabs.uri.json

import io.lemonlabs.uri.inet.Trie
import spray.json.DefaultJsonProtocol._
import spray.json._

case object SprayJsonSupport extends JsonSupport {
  implicit lazy val trieFmt: JsonFormat[Trie] = lazyFormat(jsonFormat(Trie.apply, "c", "e"))

  lazy val publicSuffixTrie: Trie = {
    publicSuffixJson.parseJson.convertTo[Trie]
  }
}
