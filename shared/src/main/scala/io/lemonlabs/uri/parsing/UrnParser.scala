package io.lemonlabs.uri.parsing

import cats.parse.Parser.Error
import cats.parse.{Parser, Parser0}
import io.lemonlabs.uri.config.UriConfig
import io.lemonlabs.uri.{Urn, UrnPath}

import scala.util.Try

class UrnParser(val input: String)(implicit conf: UriConfig = UriConfig.default) extends UriParser {
  def _nid: Parser0[String] =
    (_alpha_num | Parser.char('-')).rep0.string

  def _nss: Parser[String] =
    (_p_char ~ (_p_char | Parser.char('/')).rep0).string

  def _urn_path: Parser[UrnPath] =
    for {
      nid <- _nid.with1
      _ <- Parser.char(':')
      nss <- _nss
    } yield extractUrnPath(nid, nss)

  def _urn: Parser[Urn] =
    Parser.string("urn:") *> _urn_path.map(extractUrn)

  val extractUrnPath = (nid: String, nss: String) => {
    if (nid.length < 2)
      throw new UriParsingException(s"URN nid '$nid' is too short. Must be at least two character long")

    if (nid.head == '-' || nid.last == '-')
      throw new UriParsingException(s"URN nid '$nid' cannot start or end with a '-'")

    UrnPath(nid, conf.pathDecoder.decode(nss))
  }

  val extractUrn = (urnPath: UrnPath) => Urn(urnPath)

  private[uri] def mapParseError[T](t: => Either[Error, T], name: => String): Try[T] =
    Try(t).flatMap {
      case Left(error) =>
        scala.util.Failure(new UriParsingException(s"Invalid $name could not be parsed. $error"))
      case Right(value) =>
        scala.util.Success(value)
    }

  def parseUrnPath(): Try[UrnPath] =
    mapParseError((_urn_path <* Parser.end).parseAll(input), "URN Path")

  def parseUrn(): Try[Urn] =
    mapParseError((_urn <* Parser.end).parseAll(input), "URN")
}

object UrnParser {
  def apply(s: CharSequence)(implicit config: UriConfig = UriConfig.default): UrnParser =
    new UrnParser(s.toString)

  def parseUrn(s: String)(implicit config: UriConfig = UriConfig.default): Try[Urn] =
    UrnParser(s).parseUrn()

  def parseUrnPath(s: String)(implicit config: UriConfig = UriConfig.default): Try[UrnPath] =
    UrnParser(s).parseUrnPath()
}
