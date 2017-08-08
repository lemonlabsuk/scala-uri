package com.netaporter.uri.inet

import spray.json.DefaultJsonProtocol._
import spray.json._

import scala.io.Source

object PublicSuffixes {
  lazy val trie = {
    implicit lazy val trieFmt: JsonFormat[Trie] = lazyFormat(jsonFormat(Trie.apply, "c", "e"))
    val trieJson = Source.fromURL(getClass.getResource("/public_suffix_trie.json"), "UTF-8")
    val trie = trieJson.mkString.parseJson.convertTo[Trie]
    trieJson.close()
    trie
  }
}
