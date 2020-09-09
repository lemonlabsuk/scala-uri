package io.lemonlabs.uri.parsing

import io.lemonlabs.uri.config.UriConfig
import io.lemonlabs.uri.{Urn, UrnPath}
import org.parboiled2.CharPredicate._
import org.parboiled2._

import scala.util.{Failure, Try}

class UrnParser(val input: ParserInput)(implicit conf: UriConfig = UriConfig.default) extends Parser with UriParser {
  def _empty: Rule0 = MATCH

  def _nid: Rule1[String] =
    rule {
      capture(zeroOrMore(AlphaNum | '-'))
    }

  def _nss: Rule1[String] =
    rule {
      capture(_p_char ~ zeroOrMore(_p_char | '/'))
    }

  def _urn_path: Rule1[UrnPath] =
    rule {
      _nid ~ ":" ~ _nss ~> extractUrnPath
    }

  def _urn: Rule1[Urn] =
    rule {
      "urn:" ~ _urn_path ~> extractUrn
    }

  val extractUrnPath = (nid: String, nss: String) => {
    if (nid.length < 2)
      throw new UriParsingException(s"URN nid '$nid' is too short. Must be at least two character long")

    if (nid.head == '-' || nid.last == '-')
      throw new UriParsingException(s"URN nid '$nid' cannot start or end with a '-'")

    UrnPath(nid, conf.pathDecoder.decode(nss))
  }

  val extractUrn = (urnPath: UrnPath) => Urn(urnPath)

  private[uri] def mapParseError[T](t: Try[T], name: => String): Try[T] =
    t.recoverWith { case pe @ ParseError(_, _, _) =>
      val detail = pe.format(input)
      Failure(new UriParsingException(s"Invalid $name could not be parsed. $detail"))
    }

  def parseUrnPath(): Try[UrnPath] =
    mapParseError(rule(_urn_path ~ EOI).run(), "URN Path")

  def parseUrn(): Try[Urn] =
    mapParseError(rule(_urn ~ EOI).run(), "URN")
}
object UrnParser {
  def apply(s: CharSequence)(implicit config: UriConfig = UriConfig.default): UrnParser =
    new UrnParser(s.toString)

  def parseUrn(s: String)(implicit config: UriConfig = UriConfig.default): Try[Urn] =
    UrnParser(s).parseUrn()

  def parseUrnPath(s: String)(implicit config: UriConfig = UriConfig.default): Try[UrnPath] =
    UrnParser(s).parseUrnPath()
}
