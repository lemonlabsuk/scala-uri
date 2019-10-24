package io.lemonlabs.uri.json

import io.circe._
import io.circe.parser._
import io.lemonlabs.uri.UriException
import io.lemonlabs.uri.inet.Trie

case object CirceSupport extends JsonSupport {

  implicit val charKeyDecoder: KeyDecoder[Char] = (key: String) => key.headOption

  implicit val trieDecoder: Decoder[Trie] = (c: HCursor) =>
    for {
      children <- c.downField("c").as[Map[Char, Trie]]
      wordEnd <- c.downField("e").as[Boolean]
    } yield {
      new Trie(children, wordEnd)
    }

  override lazy val publicSuffixTrie: Trie =
    decode[Trie](publicSuffixJson).getOrElse(throw new UriException("Unable to parse public suffix JSON"))
}
