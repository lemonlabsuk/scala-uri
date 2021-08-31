package io.lemonlabs.uri.parsing

import cats.parse.Numbers.digit
import cats.parse.Parser
import cats.parse.Rfc5234.alpha
import io.lemonlabs.uri.Uri
import io.lemonlabs.uri.config.UriConfig

import scala.util.Try

trait UriParser {
  val _alpha_num: Parser[Char] = alpha | digit
  val _hex_digit: Parser[Char] = Parser.charIn(('a' to 'f') ++ ('A' to 'F')) | digit
  val _unreserved: Parser[Char] = _alpha_num | Parser.charIn("-._~")
  val _pct_encoded: Parser[Char] = _hex_digit | Parser.charIn('%')
  val _sub_delims: Parser[Char] = Parser.charIn("!$&'()*+,;=")
  val _p_char: Parser[Char] = _unreserved | _pct_encoded | _sub_delims | Parser.charIn(":@")
}

object UriParser {
  def parseUri(s: String)(implicit config: UriConfig = UriConfig.default): Try[Uri] =
    UrnParser.parseUrn(s) orElse UrlParser.parseUrl(s)
}
