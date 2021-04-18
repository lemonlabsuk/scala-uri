package io.lemonlabs.uri.parsing

import io.lemonlabs.uri.Uri
import io.lemonlabs.uri.config.UriConfig
import org.parboiled2.CharPredicate
import org.parboiled2.CharPredicate.{AlphaNum, HexDigit}
import cats.parse.{Parser0, Parser => P, Numbers}

import scala.util.Try

trait UriParser {
  val _unreserved: CharPredicate =
    AlphaNum ++ CharPredicate("-._~")

  val _pct_encoded: CharPredicate =
    HexDigit ++ CharPredicate('%')

  val _sub_delims: CharPredicate =
    CharPredicate("!$&'()*+,;=")

  val _p_char: CharPredicate =
    _unreserved ++ _pct_encoded ++ _sub_delims ++ CharPredicate(":@")
}

object UriParser {
  def parseUri(s: String)(implicit config: UriConfig = UriConfig.default): Try[Uri] =
    UrnParser.parseUrn(s) orElse UrlParser.parseUrl(s)
}
