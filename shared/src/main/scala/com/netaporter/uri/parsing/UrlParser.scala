package com.netaporter.uri.parsing

import com.netaporter.uri._
import com.netaporter.uri.config.UriConfig
import org.parboiled2.CharPredicate._
import org.parboiled2._

import scala.collection.immutable
import scala.util.{Failure, Success, Try}

class UrlParser(val input: ParserInput)(implicit conf: UriConfig = UriConfig.default) extends Parser {

  def _int: Rule1[Int] = rule {
    capture(oneOrMore(Digit)) ~> extractInt
  }

  def _scheme: Rule1[String] = rule {
    capture(Alpha ~ zeroOrMore(AlphaNum | anyOf("+-.")))
  }

  def _ip_v4: Rule1[IpV4] = rule {
    _int ~ '.' ~ _int ~ '.' ~ _int ~ '.' ~ _int ~> extractIpv4
  }

  def _ip_v6_hex_piece: Rule1[String] = rule {
    capture(oneOrMore(HexDigit))
  }

  def _full_ip_v6: Rule1[IpV6] = rule {
    '[' ~ 8.times(_ip_v6_hex_piece).separatedBy(':') ~ ']' ~> extractFullIpv6
  }

  def _ip_v6_hex_pieces: Rule1[immutable.Seq[String]] = rule {
    zeroOrMore(_ip_v6_hex_piece).separatedBy(':')
  }

  def _ip_v6_with_eluded: Rule1[IpV6] = rule {
    '[' ~ _ip_v6_hex_pieces ~ "::" ~ _ip_v6_hex_pieces ~ ']' ~> extractIpv6WithEluded
  }

  def _ip_v6: Rule1[IpV6] = rule {
    _full_ip_v6 | _ip_v6_with_eluded
  }

  def _domain_name: Rule1[DomainName] = rule {
    capture(oneOrMore(noneOf(":/?#"))) ~> extractDomainName
  }

  def _host: Rule1[Host] = rule {
    _ip_v4 | _ip_v6 | _domain_name
  }

  def _userInfo: Rule1[UserInfo] = rule {
    capture(oneOrMore(noneOf(":/?[]@"))) ~ optional(":" ~ capture(zeroOrMore(noneOf("@")))) ~ "@" ~> extractUserInfo
  }

  def _port: Rule1[Int] = rule {
    ":" ~ _int
  }

  def _authority: Rule1[Authority] = rule {
    //"//" ~ ((optional(_userInfo) ~ _host ~ optional(_port)) | (push[Option[UserInfo]](None) ~ _host ~ optional(_port))) ~> extractAuthority
    "//" ~ (optional(_userInfo) ~ _host ~ optional(_port)) ~> extractAuthority
  }

  def _authorityAsOpt: Rule1[Option[Authority]] = rule {
    _authority ~> ((a: Authority) => Some(a))
  }

  def _pathSegment: Rule1[String] = rule {
    capture(zeroOrMore(!anyOf("/?#") ~ ANY)) ~> extractPathPart
  }

  /**
    * A sequence of path parts that MUST start with a slash
    *
    * If a URI contains an authority component, then the path component must either be empty
    * or begin with a slash ("/") character.
    */
  def _pathForAuthority: Rule1[UrlPath] = rule {
    zeroOrMore("/" ~ _pathSegment) ~> extractAbsPath
  }

  def _authorityWithPath: Rule2[Option[Authority], UrlPath] = rule {
    _authorityAsOpt ~ _pathForAuthority
  }

  def _noAuthorityWithPath: Rule2[Option[Authority], UrlPath] = rule {
    push[Option[Authority]](None) ~ _relPath
  }

  /**
   * A sequence of path parts optionally starting with a slash
   */
  def _relPath: Rule1[UrlPath] = rule {
    capture(optional("/")) ~ zeroOrMore(_pathSegment).separatedBy("/") ~> extractRelPath
  }

  def _queryParam: Rule1[(String, Some[String])] = rule {
    capture(zeroOrMore(!anyOf("=&#") ~ ANY)) ~ "=" ~ capture(zeroOrMore(!anyOf("&#") ~ ANY)) ~> extractTuple
  }

  def _queryTok: Rule1[(String, None.type)] = rule {
    capture(zeroOrMore(!anyOf("=&#") ~ ANY)) ~> extractTok
  }

  def _queryParamOrTok: Rule1[(String, Option[String])] = rule {
    _queryParam | _queryTok
  }

  def _queryString: Rule1[QueryString] = rule {
    "?" ~ zeroOrMore(_queryParamOrTok).separatedBy("&") ~> extractQueryString
  }

  def _fragment: Rule1[String] = rule {
    "#" ~ capture(zeroOrMore(ANY)) ~> extractFragment
  }

  def _abs_url: Rule1[Url] = rule {
    _scheme ~ ":" ~ (_authorityWithPath | _noAuthorityWithPath) ~ optional(_queryString) ~ optional(_fragment) ~> extractAbsUri
  }

  def _protocol_rel_url: Rule1[Url] = rule {
    _authority ~ _pathForAuthority ~ optional(_queryString) ~ optional(_fragment) ~> extractProtocolRelUri
  }

  def _rel_url: Rule1[Url] = rule {
    _relPath ~ optional(_queryString) ~ optional(_fragment) ~> extractRelUri
  }

  def _url: Rule1[Url] = rule {
    (_abs_url | _protocol_rel_url | _rel_url) ~ EOI
  }

  val extractAbsUri = (scheme: String, authority: Option[Authority], path: UrlPath, qs: Option[QueryString], f: Option[String]) =>
    extractUrl (
      scheme = Some(scheme),
      authority = authority,
      path = path,
      maybeQuery = qs,
      fragment = f
    )

  val extractProtocolRelUri = (authority: Authority, path: UrlPath, qs: Option[QueryString], f: Option[String]) =>
    extractUrl (
      authority = Some(authority),
      path = path,
      maybeQuery = qs,
      fragment = f
    )

  val extractRelUri = (path: UrlPath, qs: Option[QueryString], f: Option[String]) =>
    extractUrl (
      path = path,
      maybeQuery = qs,
      fragment = f
    )

  val extractInt = (num: String) =>
    num.toInt

  val extractHexToInt = (num: String) =>
    Integer.parseInt(num, 16)

  val extractIpv4 = (a: Int, b: Int, c: Int, d: Int) =>
    IpV4(a, b, c, d)

//  val extractIpv6HexPiece = (hex: String) =>
//    HexPiece(hex)

  val extractFullIpv6 = (pieces: immutable.Seq[String]) =>
    IpV6.fromHexPieces(pieces)

  val extractIpv6WithEluded = (beforeEluded: immutable.Seq[String], afterEluded: immutable.Seq[String]) => {
    val eladedPieces = 8 - beforeEluded.size - afterEluded.size
    if(eladedPieces < 2) {
      throw new UriParsingException("IPv6 has too many pieces. Must be either exactly eight hex pieces or fewer than six hex pieces with a '::'")
    }
    IpV6.fromHexPieces(
      beforeEluded ++ Vector.fill(eladedPieces)("0") ++ afterEluded
    )
  }

//  val extractIpv6 = (pieces: immutable.Seq[IpV6Piece]) => {
//    val eladedPieces = pieces.count(_.isElided)
//    if(eladedPieces > 1) {
//      throw new UriParsingException("IPv6 address cannot have multiple '::' pieces")
//    }
//    val expandEladedTo = 8 - pieces.length - eladedPieces
//    val expandedPieces = pieces.flatMap {
//      case Elided => Vector.fill(expandEladedTo)("0")
//      case HexPiece(hex) => Vector(hex)
//    }
//    IpV6.fromHexPieces(expandedPieces)
//  }

  val extractDomainName = (domainName: String) =>
    DomainName(domainName)

  val extractUserInfo = (user: String, pass: Option[String]) =>
    UserInfo(Some(pathDecoder.decode(user)), pass.map(pathDecoder.decode))

  val extractAuthority = (userInfo: Option[UserInfo], host: Host, port: Option[Int]) =>
    Authority(userInfo.getOrElse(UserInfo.empty), host, port)

  val extractFragment = (x: String) =>
    fragmentDecoder.decode(x)

  val extractQueryString = (tuples: immutable.Seq[(String, Option[String])]) =>
    QueryString(tuples.toVector.map(queryDecoder.decodeTuple))

  val extractPathPart = (pathPart: String) =>
    pathDecoder.decode(pathPart)

  val extractAbsPath = (pp: Seq[String]) =>
    UrlPath(pp.toVector)

  val extractRelPath = (maybeSlash: String, pp: immutable.Seq[String]) =>
    UrlPath(pp.toVector, maybeSlash.nonEmpty)

  val extractTuple = (k: String, v: String) =>
    k -> Some(v)

  val extractTok = (k: String) => k -> None

  def extractUrl(scheme: Option[String] = None,
                 authority: Option[Authority] = None,
                 path: UrlPath,
                 maybeQuery: Option[QueryString],
                 fragment: Option[String]): Url = {
    // TODO: Break out into separate rules
    val query = maybeQuery.getOrElse(QueryString.empty)

    (scheme, authority, path, query, fragment) match {

//      case (Some("urn"), _, _, _, _) =>
//        Urn(UrnPath(path.toString().takeWhile(_ != ':'), path.toString().dropWhile(_ != ':').tail))

      case (Some(scheme), Some(authority), _, _, _) =>
        AbsoluteUrl(scheme, authority, path, query, fragment)

      case (None, Some(authority), _, _, _) =>
        ProtocolRelativeUrl(authority, path, query, fragment)

      case (Some(scheme), None, _, _, _) =>
        UrlWithoutAuthority(scheme, path, query, fragment)

      case (None, None, _, _, _) =>
        RelativeUrl(path, query, fragment)
    }
  }

  def pathDecoder = conf.pathDecoder
  def queryDecoder = conf.queryDecoder
  def fragmentDecoder = conf.fragmentDecoder

  def parseIpV6(): IpV6 =
    extractResult(_ip_v6.run(), "IPv6")

  def parseIpV4(): IpV4 =
    extractResult(_ip_v4.run(), "IPv4")

  def parseDomainName(): DomainName =
    extractResult(_domain_name.run(), "Domain Name")

  def parseHost(): Host =
    extractResult(_host.run(), "Host")

  def parseUserInfo(): UserInfo =
    extractResult(_userInfo.run(), "User Info")

  def parseUrn(): Urn = ???

  def parseUrnPath(): UrnPath = ???

  def parseUrlWithoutAuthority(): UrlWithoutAuthority = ???

  def parseAbsoluteUrl(): AbsoluteUrl = ???

  def parseProtocolRelativeUrl(): ProtocolRelativeUrl = ???

  def parseUrlWithAuthority(): UrlWithAuthority = ???

  def parseRelativeUrl(): RelativeUrl = ???

  def parseUri(): Uri = ???

  def parsePath(): UrlPath =
    extractResult(_relPath.run(), "Path")

  def parseAuthority(): Authority =
    extractResult(_authority.run(), "Authority")

  def parseUrl(): Url =
    extractResult(_url.run(), "URL")

  def parseQuery(): QueryString =
    extractResult(_queryString.run(), "Query String")

  def parseQueryParam(): (String, Option[String]) =
    extractResult(_queryParamOrTok.run(), "Query Parameter")
  
  private def extractResult[T](res: Try[T], name: => String): T = {
    res match {
      case Success(thing) =>
        thing

      case Failure(pe @ ParseError(_, _, _)) =>
        val detail = pe.format(input)
        throw new UriParsingException(s"Invalid $name could not be parsed. $detail")

      case Failure(e) =>
        throw e
    }
  }
}

object UrlParser {

  def apply(s: CharSequence): UrlParser =
    new UrlParser(s.toString)

  def parseIpV6(s: String)(implicit config: UriConfig = UriConfig.default): IpV6 =
    UrlParser(s).parseIpV6()

  def parseIpV4(s: String)(implicit config: UriConfig = UriConfig.default): IpV4 =
    UrlParser(s).parseIpV4()

  def parseDomainName(s: String)(implicit config: UriConfig = UriConfig.default): DomainName =
    UrlParser(s).parseDomainName()

  def parseHost(s: String)(implicit config: UriConfig = UriConfig.default): Host =
    UrlParser(s).parseHost()

  def parseUserInfo(s: String)(implicit config: UriConfig = UriConfig.default): UserInfo =
    UrlParser(s).parseUserInfo()

  def parseUrn(s: String)(implicit config: UriConfig = UriConfig.default): Urn =
    UrlParser(s).parseUrn()

  def parseUrnPath(s: String)(implicit config: UriConfig = UriConfig.default): UrnPath =
    UrlParser(s).parseUrnPath()

  def parseUrlWithoutAuthority(s: String)(implicit config: UriConfig = UriConfig.default): UrlWithoutAuthority =
    UrlParser(s).parseUrlWithoutAuthority()

  def parseAbsoluteUrl(s: String)(implicit config: UriConfig = UriConfig.default): AbsoluteUrl =
    UrlParser(s).parseAbsoluteUrl()

  def parseProtocolRelativeUrl(s: String)(implicit config: UriConfig = UriConfig.default): ProtocolRelativeUrl =
    UrlParser(s).parseProtocolRelativeUrl()

  def parseUrlWithAuthority(s: String)(implicit config: UriConfig = UriConfig.default): UrlWithAuthority =
    UrlParser(s).parseUrlWithAuthority()

  def parseRelativeUrl(s: String)(implicit config: UriConfig = UriConfig.default): RelativeUrl =
    UrlParser(s).parseRelativeUrl()

  def parsePath(s: String)(implicit config: UriConfig = UriConfig.default): UrlPath =
    UrlParser(s).parsePath()

  def parseAuthority(s: String)(implicit config: UriConfig = UriConfig.default): Authority =
    UrlParser(s).parseAuthority()

  def parseUri(toString: String): Uri =
    ???

  def parseUrl(s: String)(implicit config: UriConfig): Url =
    UrlParser(s).parseUrl()

  def parseQuery(s: String)(implicit config: UriConfig) = {
    val withQuestionMark = if(s.head == '?') s else "?" + s
    UrlParser(withQuestionMark).parseQuery()
  }

  def parseQueryParam(s: String)(implicit config: UriConfig): (String, Option[String]) =
    UrlParser(s).parseQueryParam()
}
