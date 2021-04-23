package io.lemonlabs.uri.parsing

import cats.parse.Numbers.digit
import cats.parse.Parser.{char, charIn, string, until, until0, Expectation}
import cats.parse.Rfc5234.alpha
import cats.syntax.contravariantSemigroupal._
import io.lemonlabs.uri._
import io.lemonlabs.uri.config.UriConfig
import cats.parse.{Numbers, Rfc5234, Parser => P, Parser0 => P0}
import cats.syntax.foldable._

import scala.collection.immutable
import scala.util.{Failure, Success, Try}

class UrlParser(val input: String)(implicit conf: UriConfig = UriConfig.default) extends UriParser {
  val _host_end = charIn(":/?# \t\r\n")

  def _int(maxLength: Int): P[Int] = {
    digit.rep(1, maxLength).map(l => extractInt(l.mkString_("")))
  }

  def _octet: P[Int] = _int(3).flatMap(((octet: Int) => P.pure(octet).filter(i => 0x00 <= i && i <= 0xff)))

  def _scheme: P[String] = {
    (alpha ~ (alpha | charIn("+-.")).rep0).map(t => (t._1 :: t._2).mkString(""))
  }

  def _ip_v4: P[IpV4] =
    (_octet <* char('.'), _octet.<*(char('.')), _octet.<*(char('.')), _octet.<*(char('.'))).tupled.map(
      extractIpv4.tupled
    )

  def hexDigit: P[Char] = charIn("0-9a-fA-F")
  def _ip_v6_hex_piece: P[String] = hexDigit.rep(4, 4).string

  def _full_ip_v6: P[IpV6] =
    _ip_v6_hex_piece.repSep(8, 8, char(':')).between(char('['), char(']')).map(nel => extractFullIpv6(nel.toList))

  private def _full_ip_v6_ls32_ip_v4: P[IpV6] = {
    (_ip_v6_hex_piece
      .repSep(6, 6, char(':'))
      .map(_.toList)
      ~ char(':')
      ~ _ip_v4)
      .map { case ((l, _), ipv4) => l -> ipv4 }
      .between(
        char('['),
        char(']')
      )
      .map(extractFullIpv6Ls32Ipv4.tupled)
  }

  def _ip_v6_hex_pieces: P0[immutable.Seq[String]] = _ip_v6_hex_piece.repSep0(0, char(':'))

  private def _ip_v6_hex_pieces_ending_colon: P0[immutable.Seq[String]] =
    (_ip_v6_hex_piece <* char(':')).rep0

  def _ip_v6_with_eluded: P[IpV6] = (_ip_v6_hex_pieces ~ string("::") ~ _ip_v6_hex_pieces).with1
    .between(char('['), char(']'))
    .map { case ((i, _), b) => extractIpv6WithEluded(i, b) }

  private def _ip_v6_ls32_ip_v4_with_elided: P[IpV6] = {
    (_ip_v6_hex_pieces ~ string("::") ~ _ip_v6_hex_pieces_ending_colon ~ _ip_v4)
      .map { case (((iph, _), o), v) => (iph, o, v) }
      .with1
      .between(
        char('['),
        char(']')
      )
      .map(
        extractIpv6Ls32Ipv4WithElided.tupled
      )

  }

  def _ip_v6: P[IpV6] = _full_ip_v6 | _ip_v6_with_eluded | _full_ip_v6_ls32_ip_v4 | _ip_v6_ls32_ip_v4_with_elided

  def _domain_name: P[DomainName] = until(_host_end).map(extractDomainName)

  def _host: P[Host] =
    _host_in_authority(charIn(""))

  /** To ensure that hosts that begin with an IP but have further leading characters are not matched as IPs,
    * we need to anchor the tail end to a character that signals the end of the host. E.g.
    *
    * The host in the URL `http://1.2.3.4.blah/` should be DomainName(1.2.3.4.blah), not IPv4(1.2.3.4)
    */
  def _ip_in_url_end: P0[Unit] = _ip_in_url_end(_host_end)

  def _ip_in_url_end(hostEndChars: P[_]): P0[Unit] = (hostEndChars.peek | P.end).withContext("ip_in_url_end")

  def _host_in_authority: P[Host] = _host_in_authority(_host_end)

  def _host_in_authority(hostEndChars: P[_]): P[Host] =
    ((_ip_v4 <* _ip_in_url_end(hostEndChars)).backtrack | _ip_v6.backtrack | _domain_name)
      .withContext("host_in_authority")

  def _user_info: P[UserInfo] = {
    val userAndPass: P0[(String, Option[String])] = until0(charIn(":/?[]@ \t\r\n")) ~ (
      char(':') *> until0(charIn("/@"))
    ).?
    (userAndPass.with1 <* char('@')).map(extractUserInfo.tupled).withContext("user_info")
  }

  def _port: P[Int] = (char(':') *> _int(10)).withContext("port")

  def _authority: P[Authority] =
    (_user_info.backtrack.?.with1 ~ _host_in_authority ~ _port.?)
      .map { case ((ui, hia), port) =>
        extractAuthority(ui, hia, port)
      }
      .withContext("authority")

  def _path_segment: P[String] = until(charIn("/?#")).map(extractPathPart).withContext("path_segment")

  /** A sequence of path parts that MUST start with a slash
    *
    * If a URI contains an authority component, then the path component must either be empty
    * or begin with a slash ("/") character.
    */
  def _path_for_authority: P0[AbsoluteOrEmptyPath] =
    char('/').? *> _path_segment.rep0.map(extractAbsOrEmptyPath).withContext("path_for_authority")

  /** A sequence of path parts optionally starting with a slash
    */
  def _path: P0[UrlPath] =
    (string("/").string.? ~ _path_segment.repSep0(char('/')).map(_.toList))
      .map(extractRelPath.tupled)
      .withContext("path")

  def _nonempty_path: P[UrlPath] =
    ((char('/').string.?.with1 ~ _path_segment.repSep(char('/')).map(_.toList))
      .map(extractRelPath.tupled)
      // TODO: Is this necessary?
      .backtrack | char('/').as(UrlPath.empty)).withContext("nonempty_path")
  def _query_param: P[(String, Some[String])] =
    ((until(charIn("=&#")) <* char('=')) ~ until0(charIn("&#"))).map(extractTuple.tupled).withContext("query_param")

  def _query_tok: P[(String, None.type)] = until(charIn("=&#")).map(extractTok).withContext("query_tok")

  def _query_param_or_tok: P[(String, Option[String])] =
    (_query_param.backtrack | _query_tok).withContext("query_param_or_tok")

  def _query_string: P0[QueryString] =
    char('?') *> _query_param_or_tok.repSep0(char('&')).map(extractQueryString).withContext("query_string")

  def _maybe_query_string: P0[QueryString] = _query_string | P.pure(QueryString.empty)

  def _fragment: P[String] = char('#') *> P.anyChar.rep0.string.map(extractFragment).withContext("fragment")

  def _abs_url: P[AbsoluteUrl] =
    ((_scheme <* string("://")) ~ _authority ~ _path_for_authority ~ _maybe_query_string ~ _fragment.?)
      .map { case ((((scheme, authority), p4a), qs), frag) => extractAbsoluteUrl(scheme, authority, p4a, qs, frag) }
      .withContext("abs_url")

  def _url_without_authority: P[UrlWithoutAuthority] = {
    (_data_url | _simple_url_without_authority).withContext("url_without_authority")
  }

  def _simple_url_without_authority: P[SimpleUrlWithoutAuthority] =
    ((_scheme <* string(":")) ~ _path ~ _maybe_query_string ~ _fragment.?)
      .map { case (((scheme, path), qs), frag) =>
        extractUrlWithoutAuthority(scheme, path, qs, frag)
      }
      .withContext("simple_url_without_authority")

  def _media_type_param: P[(String, String)] =
    ((until(charIn(";,=")) <* string("=")) ~ (until(charIn(";,")) <* char(';').?))
      .map(extractMediaTypeParam.tupled)
      .withContext("media_type_param")

  /*
   * https://tools.ietf.org/html/rfc1341
   */
  def _media_type: P[MediaType] = {
    (until(charIn(";,")).<*(char(';').?) ~ _media_type_param.rep0)
      .map(extractMediaType.tupled)
      .withContext("media_type")
  }

  def _data_url_base64: P[DataUrl] = {
    (string("data:") *> _media_type.<*(string("base64,")) ~ P.charsWhile(_ => true))
      .map(extractBase64DataUrl.tupled)
  }

  def _data_url_percent_encoded: P[DataUrl] = {
    (string("data:") *> (_media_type <* string(",")) ~ P.charsWhile(_ => true))
      .map(extractPercentEncodedDataUrl.tupled)
  }

  def _data_url: P[DataUrl] = {
    (_data_url_base64 | _data_url_percent_encoded).withContext("data_url")
  }

  def _protocol_rel_url: P[ProtocolRelativeUrl] =
    ((string("//") *> _authority) ~ _path_for_authority ~ _maybe_query_string ~ _fragment.?)
      .map { case (((auth, p4a), qs), frag) =>
        extractProtocolRelativeUrl(auth, p4a, qs, frag)
      }
      .withContext("protocol_rel_url")

  def _rel_url: P[RelativeUrl] = (_nonempty_path ~ _maybe_query_string ~ _fragment.?)
    .map { case ((path, qs), frag) =>
      extractRelativeUrl(path, qs, frag)
    }
    .withContext("rel_url")

  def _url_with_authority: P[UrlWithAuthority] = {
    (_abs_url | _protocol_rel_url).withContext("url_with_authority")
  }

  def _url: P[Url] =
    (_abs_url.backtrack | _protocol_rel_url.backtrack | _url_without_authority.backtrack | _rel_url).withContext("url")

  def _scp_like_user: P0[Option[String]] = {
    (until(char('@')) <* string("@")).?
  }

  // From `man scp`: [user@]host:[path]
  def _scp_like_url: P[ScpLikeUrl] =
    (_scp_like_user.with1 ~ _host_in_authority(hostEndChars = char(':')) ~ (string(":") *> _path))
      .map { case ((slu, host), path) => extractScpLikeUrl(slu, host, path) }

  val extractAbsoluteUrl =
    (scheme: String, authority: Authority, path: AbsoluteOrEmptyPath, qs: QueryString, f: Option[String]) =>
      AbsoluteUrl(scheme, authority, path, qs, f)

  val extractProtocolRelativeUrl =
    (authority: Authority, path: AbsoluteOrEmptyPath, qs: QueryString, f: Option[String]) =>
      ProtocolRelativeUrl(authority, path, qs, f)

  val extractRelativeUrl = (path: UrlPath, qs: QueryString, f: Option[String]) => RelativeUrl(path, qs, f)

  val extractUrlWithoutAuthority = (scheme: String, path: UrlPath, qs: QueryString, f: Option[String]) =>
    SimpleUrlWithoutAuthority(scheme, path, qs, f)

  val extractInt = (num: String) => num.toInt

  val extractHexToInt = (num: String) => Integer.parseInt(num, 16)

  val extractIpv4: (Int, Int, Int, Int) => IpV4 = { case (a: Int, b: Int, c: Int, d: Int) => IpV4(a, b, c, d) }

  val extractFullIpv6 = (pieces: immutable.Seq[String]) => IpV6.fromHexPieces(pieces)

  private val extractFullIpv6Ls32Ipv4 = (pieces: immutable.Seq[String], ipV4: IpV4) =>
    IpV6.fromHexPiecesAndIpV4(pieces, ipV4)

  private val extractIpv6Ls32Ipv4WithElided =
    (beforeElided: immutable.Seq[String], afterElided: immutable.Seq[String], ipV4: IpV4) => {
      val elidedPieces = 6 - beforeElided.size - afterElided.size
      if (elidedPieces < 1) {
        throw new UriParsingException(
          "IPv6 has too many pieces. When the least-significant 32bits are an IPv4, there must be either exactly six leading hex pieces or fewer than six hex pieces with a '::'"
        )
      }
      IpV6.fromHexPiecesAndIpV4(
        beforeElided ++ Vector.fill(elidedPieces)("0") ++ afterElided,
        ipV4
      )
    }

  val extractIpv6WithEluded = (beforeEluded: immutable.Seq[String], afterEluded: immutable.Seq[String]) => {
    val elidedPieces = 8 - beforeEluded.size - afterEluded.size
    if (elidedPieces < 1) {
      throw new UriParsingException(
        "IPv6 has too many pieces. Must be either exactly eight hex pieces or fewer than eight hex pieces with a '::'"
      )
    }
    IpV6.fromHexPieces(
      beforeEluded ++ Vector.fill(elidedPieces)("0") ++ afterEluded
    )
  }

  val extractDomainName = (domainName: String) => DomainName(domainName)

  val extractUserInfo = (user: String, pass: Option[String]) =>
    UserInfo(pathDecoder.decode(user), pass.map(pathDecoder.decode))

  val extractAuthority = (userInfo: Option[UserInfo], host: Host, port: Option[Int]) => Authority(userInfo, host, port)

  val extractFragment = (x: String) => fragmentDecoder.decode(x)

  val extractQueryString = (tuples: immutable.Seq[(String, Option[String])]) =>
    QueryString(tuples.toVector.map(queryDecoder.decodeTuple))

  val extractPathPart = (pathPart: String) => pathDecoder.decode(pathPart)

  val extractAbsOrEmptyPath = (pp: immutable.Seq[String]) =>
    if (pp.isEmpty) EmptyPath
    else AbsolutePath(pp.toVector)

  val extractRelPath = (maybeSlash: Option[String], pp: List[String]) =>
    if (maybeSlash.nonEmpty)
      AbsolutePath(pp.toVector)
    else if (pp == Seq(""))
      UrlPath.empty
    else
      RootlessPath(pp.toVector)

  val extractMediaTypeParam = (k: String, v: String) => k -> v

  val extractMediaType = (value: String, params: immutable.Seq[(String, String)]) => {
    MediaType(if (value.isEmpty) None else Some(value), params.toVector)
  }

  val extractBase64DataUrl = (mediaType: MediaType, data: String) => DataUrl.fromBase64(mediaType, data)

  val extractPercentEncodedDataUrl = (mediaType: MediaType, data: String) => DataUrl.fromPercentEncoded(mediaType, data)

  val extractTuple = (k: String, v: String) => k -> Some(v)

  val extractTok = (k: String) => k -> None

  val extractScpLikeUrl = (user: Option[String], host: Host, path: UrlPath) => ScpLikeUrl(user, host, path)

  def pathDecoder = conf.pathDecoder
  def queryDecoder = conf.queryDecoder
  def fragmentDecoder = conf.fragmentDecoder

  def formatExpectation(e: Expectation): String = e match {
    case e @ Expectation.OneOfStr(offset, strs) => e.toString
    case Expectation.InRange(offset, lower, upper) =>
      s"Character $offset in ASCII range '$lower' (${lower.toInt}) - '$upper' (${upper.toInt})"
    case e @ Expectation.StartOfString(offset)              => e.toString
    case e @ Expectation.EndOfString(offset, length)        => e.toString
    case e @ Expectation.Length(offset, expected, actual)   => e.toString
    case e @ Expectation.ExpectedFailureAt(offset, matched) => e.toString
    case e @ Expectation.Fail(offset)                       => e.toString
    case e @ Expectation.FailWith(offset, message)          => e.toString
    case e @ Expectation.WithContext(contextStr, expectation) =>
      s"In $contextStr: ${formatExpectation(expectation)}"
  }
  private[uri] def mapParseError[T](t: Either[P.Error, T], name: => String): Try[T] =
    t match {
      case Right(parsed) => Success(parsed)
      case Left(pe: P.Error) =>
        val detail = pe.expected
          .map(formatExpectation)
          .mkString_(", ")

        Failure(new UriParsingException(s"""Invalid $name could not be parsed. Error is occurring here ${input.take(
          pe.failedAtOffset
        )}░${input.drop(pe.failedAtOffset)}\nExpected: ${detail}"""))
    }

  def parseIpV6(): Try[IpV6] =
    mapParseError((_ip_v6 <* P.end).parseAll(input), "IPv6")

  def parseIpV4(): Try[IpV4] =
    mapParseError((_ip_v4 <* P.end).parseAll(input), "IPv4")

  def parseDomainName(): Try[DomainName] =
    mapParseError((_domain_name <* P.end).parseAll(input), "Domain Name")

  def parseHostInAuthority(): Try[Host] =
    mapParseError((_host_in_authority <* P.end).parseAll(input), "Host in Authority")

  def parseHost(): Try[Host] =
    mapParseError((_host <* P.end).parseAll(input), "Host")

  def parseUserInfo(): Try[UserInfo] =
    mapParseError((_user_info <* P.end).parseAll(input), "User Info")

  def parseUrlWithoutAuthority(): Try[UrlWithoutAuthority] =
    mapParseError((_url_without_authority <* P.end).parseAll(input), "Url")

  def parseSimpleUrlWithoutAuthority(): Try[SimpleUrlWithoutAuthority] =
    mapParseError((_simple_url_without_authority <* P.end).parseAll(input), "Url")

  def parseDataUrl(): Try[DataUrl] =
    mapParseError((_data_url <* P.end).parseAll(input), "Data Url")

  def parseScpLikeUrl(): Try[ScpLikeUrl] =
    mapParseError((_scp_like_url <* P.end).parseAll(input), "scp-like Url")

  def parseAbsoluteUrl(): Try[AbsoluteUrl] =
    mapParseError((_abs_url <* P.end).parseAll(input), "Url")

  def parseProtocolRelativeUrl(): Try[ProtocolRelativeUrl] =
    mapParseError((_protocol_rel_url <* P.end).parseAll(input), "Url")

  def parseUrlWithAuthority(): Try[UrlWithAuthority] =
    mapParseError((_url_with_authority <* P.end).parseAll(input), "Url")

  def parseRelativeUrl(): Try[RelativeUrl] =
    mapParseError((_rel_url <* P.end).parseAll(input), "Url")

  def parsePath(): Try[UrlPath] =
    mapParseError((_path <* P.end).parseAll(input), "Path")

  def parseAuthority(): Try[Authority] =
    mapParseError((_authority <* P.end).parseAll(input), "Authority")

  def parseUrl(): Try[Url] =
    mapParseError((_url <* P.end).parseAll(input), "URL")

  def parseQuery(): Try[QueryString] =
    mapParseError((_query_string <* P.end).parseAll(input), "Query String")

  def parseQueryParam(): Try[(String, Option[String])] =
    mapParseError((_query_param_or_tok <* P.end).parseAll(input), "Query Parameter")
}

object UrlParser {
  def apply(s: CharSequence)(implicit config: UriConfig = UriConfig.default): UrlParser =
    new UrlParser(s.toString)

  def parseIpV6(s: String)(implicit config: UriConfig = UriConfig.default): Try[IpV6] =
    UrlParser(s).parseIpV6()

  def parseIpV4(s: String)(implicit config: UriConfig = UriConfig.default): Try[IpV4] =
    UrlParser(s).parseIpV4()

  def parseDomainName(s: String)(implicit config: UriConfig = UriConfig.default): Try[DomainName] =
    UrlParser(s).parseDomainName()

  def parseHost(s: String)(implicit config: UriConfig = UriConfig.default): Try[Host] =
    UrlParser(s).parseHost()

  def parseUserInfo(s: String)(implicit config: UriConfig = UriConfig.default): Try[UserInfo] =
    UrlParser(s + "@").parseUserInfo()

  def parseUrlWithoutAuthority(s: String)(implicit config: UriConfig = UriConfig.default): Try[UrlWithoutAuthority] =
    UrlParser(s).parseUrlWithoutAuthority()

  def parseSimpleUrlWithoutAuthority(
      s: String
  )(implicit config: UriConfig = UriConfig.default): Try[SimpleUrlWithoutAuthority] =
    UrlParser(s).parseSimpleUrlWithoutAuthority()

  // Data URLs may be formatted with newlines, so strip them
  def parseDataUrl(s: String)(implicit config: UriConfig = UriConfig.default): Try[DataUrl] =
    UrlParser(s.replace("\n", "")).parseDataUrl()

  def parseScpLikeUrl(s: String)(implicit config: UriConfig = UriConfig.default): Try[ScpLikeUrl] =
    UrlParser(s).parseScpLikeUrl()

  def parseAbsoluteUrl(s: String)(implicit config: UriConfig = UriConfig.default): Try[AbsoluteUrl] =
    UrlParser(s).parseAbsoluteUrl()

  def parseProtocolRelativeUrl(s: String)(implicit config: UriConfig = UriConfig.default): Try[ProtocolRelativeUrl] =
    UrlParser(s).parseProtocolRelativeUrl()

  def parseUrlWithAuthority(s: String)(implicit config: UriConfig = UriConfig.default): Try[UrlWithAuthority] =
    UrlParser(s).parseUrlWithAuthority()

  def parseRelativeUrl(s: String)(implicit config: UriConfig = UriConfig.default): Try[RelativeUrl] =
    UrlParser(s).parseRelativeUrl()

  def parsePath(s: String)(implicit config: UriConfig = UriConfig.default): Try[UrlPath] =
    UrlParser(s).parsePath()

  def parseAuthority(s: String)(implicit config: UriConfig = UriConfig.default): Try[Authority] =
    UrlParser(s).parseAuthority()

  def parseUrl(s: String)(implicit config: UriConfig = UriConfig.default): Try[Url] =
    UrlParser(s).parseUrl()

  def parseQuery(s: String)(implicit config: UriConfig = UriConfig.default): Try[QueryString] = {
    val withQuestionMark = if (s.head == '?') s else "?" + s
    UrlParser(withQuestionMark).parseQuery()
  }

  def parseQueryParam(s: String)(implicit config: UriConfig = UriConfig.default): Try[(String, Option[String])] =
    UrlParser(s).parseQueryParam()
}
