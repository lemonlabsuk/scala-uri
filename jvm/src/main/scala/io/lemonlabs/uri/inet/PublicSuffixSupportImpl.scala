package io.lemonlabs.uri.inet

import io.lemonlabs.uri.Host
import spray.json.DefaultJsonProtocol._
import spray.json._

import scala.io.Source

object PublicSuffixSupportImpl {
  lazy val trie: Trie = {
    implicit lazy val trieFmt: JsonFormat[Trie] = lazyFormat(jsonFormat(Trie.apply, "c", "e"))
    val trieJson = Source.fromURL(getClass.getResource("/public_suffix_trie.json"), "UTF-8")
    val trie = trieJson.mkString.parseJson.convertTo[Trie]
    trieJson.close()
    trie
  }
}

trait PublicSuffixSupportImpl { this: Host =>
  /**
    * Returns the longest public suffix for the host in this URI. Examples include:
    *  `com`   for `www.example.com`
    *  `co.uk` for `www.example.co.uk`
    *
    * @return the longest public suffix for the host in this URI
    */
  def publicSuffix: Option[String] =
    PublicSuffixSupportImpl.trie.longestMatch(value.reverse).map(_.reverse)

  /**
    * Returns all longest public suffixes for the host in this URI. Examples include:
    *  `com` for `www.example.com`
    *  `co.uk` and `uk` for `www.example.co.uk`
    *
    * @return all public suffixes for the host in this URI
    */
  def publicSuffixes: Vector[String] =
    PublicSuffixSupportImpl.trie.matches(value.reverse).map(_.reverse)
}
