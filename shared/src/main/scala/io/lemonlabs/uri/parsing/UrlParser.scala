package io.lemonlabs.uri.parsing

import cats.parse.Numbers.digit
import cats.parse.Parser._
import cats.parse.Rfc5234.alpha
import cats.parse.{Parser, Parser0}
import io.lemonlabs.uri._
import io.lemonlabs.uri.config.UriConfig

import scala.collection.immutable
import scala.util.{Success, Try}

class UrlParser(val input: String)(implicit conf: UriConfig = UriConfig.default) extends UriParser {
  val _host_end = ":/?# \t\r\n"

  def _int(maxLength: Int): Parser[Int] =
    digit.rep(1, maxLength).string.map(extractInt)

  def _octet: Parser[Int] =
    _int(maxLength = 3).filter(octet => 0x00 <= octet && octet <= 0xff)

  def _scheme: Parser[String] =
    (alpha ~ (alpha | digit | charIn('+', '-', '.')).rep0).string

  def _ip_v4: Parser[IpV4] =
    _octet.repSep(min = 4, max = 4, sep = char('.')).map { list =>
      val List(a, b, c, d) = list.toList
      extractIpv4(a, b, c, d)
    }

  def _ip_v6_hex_piece: Parser[String] =
    _hex_digit.rep(1, 4).string

  def _full_ip_v6: Parser[IpV6] =
    for {
      _ <- char('[')
      ip_v6_hex_pieces <- _ip_v6_hex_piece.repSep(8, 8, char(':'))
      _ <- char(']')
    } yield extractFullIpv6(ip_v6_hex_pieces.toList)

  private def _full_ip_v6_ls32_ip_v4: Parser[IpV6] =
    for {
      _ <- char('[')
      ip_v6_hex_pieces <- _ip_v6_hex_piece.repSep(6, 6, char(':'))
      _ <- char(':')
      ip_v4 <- _ip_v4
      _ <- char(']')
    } yield extractFullIpv6Ls32Ipv4(ip_v6_hex_pieces.toList, ip_v4)

  def _ip_v6_hex_pieces: Parser0[immutable.Seq[String]] =
    _ip_v6_hex_piece.repSep0(char(':')).map(_.toList)

  private def _ip_v6_hex_pieces_ending_colon: Parser0[immutable.Seq[String]] =
    (_ip_v6_hex_piece.soft <* char(':')).rep0.map(_.toList)

  def _ip_v6_with_eluded: Parser[IpV6] =
    for {
      _ <- char('[')
      firstPart <- _ip_v6_hex_pieces
      _ <- string("::")
      secondPart <- _ip_v6_hex_pieces
      _ <- char(']')
    } yield extractIpv6WithEluded(firstPart, secondPart)

  private def _ip_v6_ls32_ip_v4_with_elided: Parser[IpV6] =
    for {
      _ <- char('[')
      firstPart <- _ip_v6_hex_pieces
      _ <- string("::")
      t <- Parser.oneOf0[(immutable.Seq[String], IpV4)](
        List(
          (_ip_v6_hex_pieces_ending_colon ~ _ip_v4).backtrack,
          Parser.pure(List.empty[String]).with1 ~ _ip_v4
        )
      )
      _ <- char(']')
    } yield extractIpv6Ls32Ipv4WithElided(firstPart.toList, t._1.toList, t._2)

  def _ip_v6: Parser[IpV6] =
    Parser.oneOf(
      List(
        _full_ip_v6.backtrack,
        _ip_v6_with_eluded.backtrack,
        _full_ip_v6_ls32_ip_v4.backtrack,
        _ip_v6_ls32_ip_v4_with_elided
      )
    )

  def _domain_name: Parser0[DomainName] =
    until0(charIn(_host_end)).map(extractDomainName)

  def _host: Parser0[Host] =
    _host_in_authority("")

  /** To ensure that hosts that begin with an IP but have further leading characters are not matched as IPs,
    * we need to anchor the tail end to a character that signals the end of the host. E.g.
    *
    * The host in the URL `http://1.2.3.4.blah/` should be DomainName(1.2.3.4.blah), not IPv4(1.2.3.4)
    */
  def _ip_in_url_end: Parser0[Unit] = _ip_in_url_end(_host_end)

  def _ip_in_url_end(hostEndChars: String): Parser0[Unit] =
    charIn(hostEndChars).peek | Parser.end

  // todo: use a default arg instead of overloading?
  def _host_in_authority: Parser0[Host] = _host_in_authority(_host_end)

  def _host_in_authority(hostEndChars: String): Parser0[Host] =
    (_ip_v4 <* _ip_in_url_end(hostEndChars)).backtrack | _ip_v6 | _domain_name

  def _user_info: Parser0[UserInfo] =
    for {
      user <- until0(charIn(":/?[]@ \t\r\n"))
      password <- (char(':') *> until0(charIn("/@"))).?
      _ <- char('@')
    } yield extractUserInfo(user, password)

  def _port: Parser[Int] = char(':') *> _int(10)

  def _authority: Parser0[Authority] =
    for {
      t <- (_user_info.map(Some.apply) ~ _host_in_authority).backtrack |
        (Parser.pure(None) ~ _host_in_authority)
      port <- _port.?
    } yield extractAuthority(t._1, t._2, port)

  def _path_segment: Parser0[String] =
    until0(charIn("/?#")).string.map(extractPathPart)

  /** A sequence of path parts that MUST start with a slash
    *
    * If a URI contains an authority component, then the path component must either be empty
    * or begin with a slash ("/") character.
    */
  def _path_for_authority: Parser0[AbsoluteOrEmptyPath] =
    (char('/') *> _path_segment).rep0.map { parts =>
      extractAbsOrEmptyPath(parts)
    }

  /** A sequence of path parts optionally starting with a slash
    */
  def _path: Parser0[UrlPath] =
    (char('/').? ~ rep0sep0(_path_segment, separator = char('/'))).map { case (maybeSlash, parts) =>
      extractRelPath(maybeSlash, parts)
    }

  def _query_param: Parser[(String, Some[String])] =
    for {
      key <- until(charIn("=&#")).string
      _ <- char('=')
      value <- until0(charIn("&#"))
    } yield extractTuple(key, value)

  def _query_tok: Parser[(String, None.type)] =
    for {
      key <- until(charIn("=&#")).string
    } yield extractTok(key)

  def _query_param_or_tok: Parser0[(String, Option[String])] =
    _query_param.backtrack | _query_tok | (char('&').peek | char('#').peek | Parser.end).as(("", None))

  def _query_string: Parser[QueryString] =
    for {
      _ <- char('?')
      params <- rep0sep0(_query_param_or_tok, char('&'))
    } yield extractQueryString(params)

  def _maybe_query_string: Parser0[QueryString] =
    _query_string | Parser.pure(QueryString.empty)

  def _fragment: Parser[String] =
    char('#') *> Parser.anyChar.rep0.string.map(extractFragment)

  def _abs_url: Parser[AbsoluteUrl] =
    for {
      scheme <- _scheme
      _ <- Parser.string("://")
      authority <- _authority
      path_for_authority <- _path_for_authority
      maybe_query_string <- _maybe_query_string
      maybeFragment <- _fragment.?
    } yield extractAbsoluteUrl(scheme, authority, path_for_authority, maybe_query_string, maybeFragment)

  def _url_without_authority: Parser[UrlWithoutAuthority] =
    _data_url.backtrack | _simple_url_without_authority

  def _simple_url_without_authority: Parser[SimpleUrlWithoutAuthority] =
    for {
      scheme <- _scheme
      _ <- char(':')
      // If a URI does not contain an authority component,
      // then the path cannot begin with two slash characters ("//")
      _ <- not(string("//"))
      path <- _path
      maybe_query_string <- _maybe_query_string
      maybe_fragment <- _fragment.?
    } yield extractUrlWithoutAuthority(scheme, path, maybe_query_string, maybe_fragment)

  def _media_type_param: Parser[(String, String)] =
    for {
      k <- Parser.until(charIn(";,=")).string
      _ <- char('=')
      v <- Parser.until(charIn(";,")).string
    } yield extractMediaTypeParam(k, v)

  /*
   * https://tools.ietf.org/html/rfc1341
   */
  def _media_type: Parser0[MediaType] =
    for {
      value <- Parser.until0(charIn(";,"))
      params <- (char(';') *> _media_type_param).backtrack.rep0
    } yield extractMediaType(value, params)

  def _data_url_base64: Parser[DataUrl] =
    for {
      _ <- Parser.string("data:")
      // If a URI does not contain an authority component,
      // then the path cannot begin with two slash characters ("//")
      _ <- not(string("//"))
      media_type <- _media_type
      _ <- Parser.string(";base64,")
      data <- Parser.until(Parser.end)
    } yield extractBase64DataUrl(media_type, data)

  def _data_url_percent_encoded: Parser[DataUrl] =
    for {
      _ <- Parser.string("data:")
      // If a URI does not contain an authority component,
      // then the path cannot begin with two slash characters ("//")
      _ <- not(string("//"))
      media_type <- _media_type
      _ <- Parser.char(';').?
      _ <- Parser.char(',')
      data <- Parser.until(Parser.end)
    } yield extractPercentEncodedDataUrl(media_type, data)

  def _data_url: Parser[DataUrl] =
    _data_url_base64.backtrack | _data_url_percent_encoded

  def _protocol_rel_url: Parser[ProtocolRelativeUrl] =
    for {
      _ <- Parser.string("//")
      authority <- _authority
      path_for_authority <- _path_for_authority
      maybe_query_string <- _maybe_query_string
      maybe_fragment <- _fragment.?
    } yield extractProtocolRelativeUrl(authority, path_for_authority, maybe_query_string, maybe_fragment)

  def _rel_url: Parser0[RelativeUrl] =
    for {
      // If a URI does not contain an authority component,
      // then the path cannot begin with two slash characters ("//")
      _ <- not(string("//"))
      path <- _path
      // In addition, a URI reference (Section 4.1) may be a relative-path reference, in which case the
      // first path segment cannot contain a colon (":") character
      colonInFirstSegment = path.nonEmptyRootless && path.parts.headOption.exists(_.contains(':'))
      _ <- if (colonInFirstSegment) Parser.fail else Parser.unit
      maybe_query_string <- _maybe_query_string
      maybe_fragment <- _fragment.?
    } yield extractRelativeUrl(path, maybe_query_string, maybe_fragment)

  def _url_with_authority: Parser[UrlWithAuthority] =
    _abs_url.backtrack | _protocol_rel_url

  def _url: Parser0[Url] =
    _abs_url.backtrack | _protocol_rel_url.backtrack | _url_without_authority.backtrack | _rel_url

  def _scp_like_user: Parser0[Option[String]] =
    (Parser.until0(char('@')).soft <* char('@')).?

  // From `man scp`: [user@]host:[path]
  def _scp_like_url: Parser[ScpLikeUrl] =
    for {
      scp_like_user <- _scp_like_user.with1
      host_in_authority <- _host_in_authority(hostEndChars = ":").with1
      _ <- char(':')
      path <- _path
    } yield extractScpLikeUrl(scp_like_user, host_in_authority, path)

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

  val extractIpv4 = (a: Int, b: Int, c: Int, d: Int) => IpV4(a, b, c, d)

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

  val extractRelPath = (maybeSlash: Option[Unit], pp: immutable.Seq[String]) =>
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

  private[uri] def mapParseError[T](t: => Either[Error, T], name: => String): Try[T] =
    Try(t).flatMap {
      case Left(error) =>
        scala.util.Failure(new UriParsingException(s"Invalid $name could not be parsed. $error"))
      case Right(value) =>
        scala.util.Success(value)
    }

  def parseIpV6(): Try[IpV6] =
    mapParseError((_ip_v6 <* Parser.end).parseAll(input), "IPv6")

  def parseIpV4(): Try[IpV4] =
    mapParseError((_ip_v4 <* Parser.end).parseAll(input), "IPv4")

  def parseDomainName(): Try[DomainName] =
    mapParseError((_domain_name <* Parser.end).parseAll(input), "Domain Name")

  def parseHost(): Try[Host] =
    mapParseError((_host <* Parser.end).parseAll(input), "Host")

  def parseUserInfo(): Try[UserInfo] =
    mapParseError((_user_info <* Parser.end).parseAll(input), "User Info")

  def parseUrlWithoutAuthority(): Try[UrlWithoutAuthority] =
    mapParseError((_url_without_authority <* Parser.end).parseAll(input), "Url")

  def parseSimpleUrlWithoutAuthority(): Try[SimpleUrlWithoutAuthority] =
    mapParseError((_simple_url_without_authority <* Parser.end).parseAll(input), "Url")

  def parseDataUrl(): Try[DataUrl] =
    mapParseError((_data_url <* Parser.end).parseAll(input), "Data Url")

  def parseScpLikeUrl(): Try[ScpLikeUrl] =
    mapParseError((_scp_like_url <* Parser.end).parseAll(input), "scp-like Url")

  def parseAbsoluteUrl(): Try[AbsoluteUrl] =
    mapParseError((_abs_url <* Parser.end).parseAll(input), "Url")

  def parseProtocolRelativeUrl(): Try[ProtocolRelativeUrl] =
    mapParseError((_protocol_rel_url <* Parser.end).parseAll(input), "Url")

  def parseUrlWithAuthority(): Try[UrlWithAuthority] =
    mapParseError((_url_with_authority <* Parser.end).parseAll(input), "Url")

  def parseRelativeUrl(): Try[RelativeUrl] =
    mapParseError((_rel_url <* Parser.end).parseAll(input), "Url")

  def parsePath(): Try[UrlPath] =
    mapParseError((_path <* Parser.end).parseAll(input), "Path")

  def parseAuthority(): Try[Authority] =
    mapParseError((_authority <* Parser.end).parseAll(input), "Authority")

  def parseUrl(): Try[Url] =
    mapParseError((_url <* Parser.end).parseAll(input), "URL")

  def parseQuery(): Try[QueryString] = {
    if (input == "?")
      Success(QueryString.empty)
    else
      mapParseError((_query_string <* Parser.end).parseAll(input), "Query String")
  }

  def parseQueryParam(): Try[(String, Option[String])] =
    mapParseError((_query_param_or_tok <* Parser.end).parseAll(input), "Query Parameter")

  private def rep0sep0[A](data: Parser0[A], separator: Parser[Any]): Parser0[List[A]] =
    (data.? ~ (separator *> data).rep0).map { case (a, as) => a ++: as }
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
    val withQuestionMark = if (s.headOption.contains('?')) s else "?" + s
    UrlParser(withQuestionMark).parseQuery()
  }

  def parseQueryParam(s: String)(implicit config: UriConfig = UriConfig.default): Try[(String, Option[String])] =
    UrlParser(s).parseQueryParam()
}
