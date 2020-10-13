package io.lemonlabs.uri.parsing

import io.lemonlabs.uri._
import io.lemonlabs.uri.config.UriConfig
import org.parboiled2.CharPredicate._
import org.parboiled2._

import scala.collection.immutable
import scala.util.{Failure, Try}

class UrlParser(val input: ParserInput)(implicit conf: UriConfig = UriConfig.default) extends Parser with UriParser {
  val _host_end = ":/?# \t\r\n"

  def _int(maxLength: Int): Rule1[Int] =
    rule {
      capture((1 to maxLength).times(Digit)) ~> extractInt
    }

  def _octet: Rule1[Int] =
    rule {
      _int(3) ~> ((octet: Int) => test(0x00 <= octet && octet <= 0xff) ~ push(octet))
    }

  def _scheme: Rule1[String] =
    rule {
      capture(Alpha ~ zeroOrMore(AlphaNum | anyOf("+-.")))
    }

  def _ip_v4: Rule1[IpV4] =
    rule {
      _octet ~ '.' ~ _octet ~ '.' ~ _octet ~ '.' ~ _octet ~> extractIpv4
    }

  def _ip_v6_hex_piece: Rule1[String] =
    rule {
      capture((1 to 4).times(HexDigit))
    }

  def _full_ip_v6: Rule1[IpV6] =
    rule {
      '[' ~ 8.times(_ip_v6_hex_piece).separatedBy(':') ~ ']' ~> extractFullIpv6
    }

  private def _full_ip_v6_ls32_ip_v4: Rule1[IpV6] =
    rule {
      '[' ~ 6.times(_ip_v6_hex_piece).separatedBy(':') ~ ':' ~ _ip_v4 ~ ']' ~> extractFullIpv6Ls32Ipv4
    }

  def _ip_v6_hex_pieces: Rule1[immutable.Seq[String]] =
    rule {
      zeroOrMore(_ip_v6_hex_piece).separatedBy(':')
    }

  private def _ip_v6_hex_pieces_ending_colon: Rule1[immutable.Seq[String]] =
    rule {
      zeroOrMore(_ip_v6_hex_piece ~ ':')
    }

  def _ip_v6_with_eluded: Rule1[IpV6] =
    rule {
      '[' ~ _ip_v6_hex_pieces ~ "::" ~ _ip_v6_hex_pieces ~ ']' ~> extractIpv6WithEluded
    }

  private def _ip_v6_ls32_ip_v4_with_elided: Rule1[IpV6] =
    rule {
      '[' ~ _ip_v6_hex_pieces ~ "::" ~ _ip_v6_hex_pieces_ending_colon ~ _ip_v4 ~ ']' ~> extractIpv6Ls32Ipv4WithElided
    }

  def _ip_v6: Rule1[IpV6] =
    rule {
      _full_ip_v6 | _ip_v6_with_eluded | _full_ip_v6_ls32_ip_v4 | _ip_v6_ls32_ip_v4_with_elided
    }

  def _domain_name: Rule1[DomainName] =
    rule {
      capture(zeroOrMore(noneOf(_host_end))) ~> extractDomainName
    }

  def _host: Rule1[Host] =
    _host_in_authority("")

  /** To ensure that hosts that begin with an IP but have further leading characters are not matched as IPs,
    * we need to anchor the tail end to a character that signals the end of the host. E.g.
    *
    * The host in the URL `http://1.2.3.4.blah/` should be DomainName(1.2.3.4.blah), not IPv4(1.2.3.4)
    */
  def _ip_in_url_end: Rule0 = _ip_in_url_end(_host_end)

  def _ip_in_url_end(hostEndChars: String): Rule0 =
    rule {
      &(anyOf(hostEndChars) | EOI)
    }

  def _host_in_authority: Rule1[Host] = _host_in_authority(_host_end)

  def _host_in_authority(hostEndChars: String): Rule1[Host] =
    rule {
      (_ip_v4 ~ _ip_in_url_end(hostEndChars)) | _ip_v6 | _domain_name
    }

  def _user_info: Rule1[UserInfo] =
    rule {
      capture(zeroOrMore(noneOf(":/?[]@ \t\r\n"))) ~ optional(
        ":" ~ capture(zeroOrMore(noneOf("/@")))
      ) ~ "@" ~> extractUserInfo
    }

  def _port: Rule1[Int] =
    rule {
      ":" ~ _int(10)
    }

  def _authority: Rule1[Authority] =
    rule {
      (optional(_user_info) ~ _host_in_authority ~ optional(_port)) ~> extractAuthority
    }

  def _path_segment: Rule1[String] =
    rule {
      capture(zeroOrMore(!anyOf("/?#") ~ ANY)) ~> extractPathPart
    }

  /** A sequence of path parts that MUST start with a slash
    *
    * If a URI contains an authority component, then the path component must either be empty
    * or begin with a slash ("/") character.
    */
  def _path_for_authority: Rule1[AbsoluteOrEmptyPath] =
    rule {
      zeroOrMore("/" ~ _path_segment) ~> extractAbsOrEmptyPath
    }

  /** A sequence of path parts optionally starting with a slash
    */
  def _path: Rule1[UrlPath] =
    rule {
      capture(optional("/")) ~ zeroOrMore(_path_segment).separatedBy("/") ~> extractRelPath
    }

  def _query_param: Rule1[(String, Some[String])] =
    rule {
      capture(zeroOrMore(!anyOf("=&#") ~ ANY)) ~ "=" ~ capture(zeroOrMore(!anyOf("&#") ~ ANY)) ~> extractTuple
    }

  def _query_tok: Rule1[(String, None.type)] =
    rule {
      capture(zeroOrMore(!anyOf("=&#") ~ ANY)) ~> extractTok
    }

  def _query_param_or_tok: Rule1[(String, Option[String])] =
    rule {
      _query_param | _query_tok
    }

  def _query_string: Rule1[QueryString] =
    rule {
      "?" ~ zeroOrMore(_query_param_or_tok).separatedBy("&") ~> extractQueryString
    }

  def _maybe_query_string: Rule1[QueryString] =
    rule {
      _query_string | push(QueryString.empty)
    }

  def _fragment: Rule1[String] =
    rule {
      "#" ~ capture(zeroOrMore(ANY)) ~> extractFragment
    }

  def _abs_url: Rule1[AbsoluteUrl] =
    rule {
      _scheme ~ "://" ~ _authority ~ _path_for_authority ~ _maybe_query_string ~ optional(
        _fragment
      ) ~> extractAbsoluteUrl
    }

  def _url_without_authority: Rule1[UrlWithoutAuthority] =
    rule {
      _data_url | _simple_url_without_authority
    }

  def _simple_url_without_authority: Rule1[SimpleUrlWithoutAuthority] =
    rule {
      _scheme ~ ":" ~ _path ~ _maybe_query_string ~ optional(_fragment) ~> extractUrlWithoutAuthority
    }

  def _media_type_param: Rule1[(String, String)] =
    rule {
      capture(zeroOrMore(noneOf(";,="))) ~ "=" ~ capture(zeroOrMore(noneOf(";,"))) ~ optional(
        ";"
      ) ~> extractMediaTypeParam
    }

  /*
   * https://tools.ietf.org/html/rfc1341
   */
  def _media_type: Rule1[MediaType] =
    rule {
      capture(zeroOrMore(noneOf(";,"))) ~ optional(";") ~ zeroOrMore(_media_type_param) ~> extractMediaType
    }

  def _data_url_base64: Rule1[DataUrl] =
    rule {
      "data:" ~ _media_type ~ "base64," ~ capture(zeroOrMore(ANY)) ~> extractBase64DataUrl
    }

  def _data_url_percent_encoded: Rule1[DataUrl] =
    rule {
      "data:" ~ _media_type ~ "," ~ capture(zeroOrMore(ANY)) ~> extractPercentEncodedDataUrl
    }

  def _data_url: Rule1[DataUrl] =
    rule {
      _data_url_base64 | _data_url_percent_encoded
    }

  def _protocol_rel_url: Rule1[ProtocolRelativeUrl] =
    rule {
      "//" ~ _authority ~ _path_for_authority ~ _maybe_query_string ~ optional(_fragment) ~> extractProtocolRelativeUrl
    }

  def _rel_url: Rule1[RelativeUrl] =
    rule {
      _path ~ _maybe_query_string ~ optional(_fragment) ~> extractRelativeUrl
    }

  def _url_with_authority: Rule1[UrlWithAuthority] =
    rule {
      _abs_url | _protocol_rel_url
    }

  def _url: Rule1[Url] =
    rule {
      _abs_url | _protocol_rel_url | _url_without_authority | _rel_url
    }

  def _scp_like_user: Rule1[Option[String]] =
    rule {
      optional(capture(zeroOrMore(noneOf("@"))) ~ "@")
    }

  // From `man scp`: [user@]host:[path]
  def _scp_like_url: Rule1[ScpLikeUrl] =
    rule {
      _scp_like_user ~ _host_in_authority(hostEndChars = ":") ~ ":" ~ _path ~> extractScpLikeUrl
    }

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

  val extractRelPath = (maybeSlash: String, pp: immutable.Seq[String]) =>
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

  private[uri] def mapParseError[T](t: Try[T], name: => String): Try[T] =
    t.recoverWith { case pe @ ParseError(_, _, _) =>
      val detail = pe.format(input)
      Failure(new UriParsingException(s"Invalid $name could not be parsed. $detail"))
    }

  def parseIpV6(): Try[IpV6] =
    mapParseError(rule(_ip_v6 ~ EOI).run(), "IPv6")

  def parseIpV4(): Try[IpV4] =
    mapParseError(rule(_ip_v4 ~ EOI).run(), "IPv4")

  def parseDomainName(): Try[DomainName] =
    mapParseError(rule(_domain_name ~ EOI).run(), "Domain Name")

  def parseHost(): Try[Host] =
    mapParseError(rule(_host ~ EOI).run(), "Host")

  def parseUserInfo(): Try[UserInfo] =
    mapParseError(rule(_user_info ~ EOI).run(), "User Info")

  def parseUrlWithoutAuthority(): Try[UrlWithoutAuthority] =
    mapParseError(rule(_url_without_authority ~ EOI).run(), "Url")

  def parseSimpleUrlWithoutAuthority(): Try[SimpleUrlWithoutAuthority] =
    mapParseError(rule(_simple_url_without_authority ~ EOI).run(), "Url")

  def parseDataUrl(): Try[DataUrl] =
    mapParseError(rule(_data_url ~ EOI).run(), "Data Url")

  def parseScpLikeUrl(): Try[ScpLikeUrl] =
    mapParseError(rule(_scp_like_url ~ EOI).run(), "scp-like Url")

  def parseAbsoluteUrl(): Try[AbsoluteUrl] =
    mapParseError(rule(_abs_url ~ EOI).run(), "Url")

  def parseProtocolRelativeUrl(): Try[ProtocolRelativeUrl] =
    mapParseError(rule(_protocol_rel_url ~ EOI).run(), "Url")

  def parseUrlWithAuthority(): Try[UrlWithAuthority] =
    mapParseError(rule(_url_with_authority ~ EOI).run(), "Url")

  def parseRelativeUrl(): Try[RelativeUrl] =
    mapParseError(rule(_rel_url ~ EOI).run(), "Url")

  def parsePath(): Try[UrlPath] =
    mapParseError(rule(_path ~ EOI).run(), "Path")

  def parseAuthority(): Try[Authority] =
    mapParseError(rule(_authority ~ EOI).run(), "Authority")

  def parseUrl(): Try[Url] =
    mapParseError(rule(_url ~ EOI).run(), "URL")

  def parseQuery(): Try[QueryString] =
    mapParseError(rule(_query_string ~ EOI).run(), "Query String")

  def parseQueryParam(): Try[(String, Option[String])] =
    mapParseError(rule(_query_param_or_tok ~ EOI).run(), "Query Parameter")
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
