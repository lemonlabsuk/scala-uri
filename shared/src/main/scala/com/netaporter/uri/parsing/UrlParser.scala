package com.netaporter.uri.parsing

import com.netaporter.uri._
import com.netaporter.uri.config.UriConfig
import org.parboiled2._

import scala.collection.immutable
import scala.util.{Failure, Success}

class UrlParser(val input: ParserInput)(implicit conf: UriConfig = UriConfig.default) extends Parser {

  def _scheme: Rule1[String] = rule {
    capture(CharPredicate.Alpha ~ zeroOrMore(CharPredicate.AlphaNum | anyOf("+-.")))
  }

  def _host_name: Rule1[String] = rule {
    capture("[" ~ oneOrMore(!anyOf("[]/?#") ~ ANY) ~ "]") | capture(oneOrMore(!anyOf(":/?#") ~ ANY))
  }

  def _userInfo: Rule1[UserInfo] = rule {
    capture(oneOrMore(!anyOf(":/?@") ~ ANY)) ~ optional(":" ~ capture(zeroOrMore(!anyOf("@") ~ ANY))) ~ "@" ~> extractUserInfo
  }

  def _port: Rule1[Int] = rule {
    ":" ~ capture(oneOrMore(CharPredicate.Digit)) ~> extractInt
  }

  def _authority: Rule1[Authority] = rule {
    "//" ~ ((optional(_userInfo) ~ _host_name ~ optional(_port)) | (push[Option[UserInfo]](None) ~ _host_name ~ optional(_port))) ~> extractAuthority
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

  val extractUserInfo = (user: String, pass: Option[String]) =>
    UserInfo(Some(pathDecoder.decode(user)), pass.map(pathDecoder.decode))

  val extractAuthority = (userInfo: Option[UserInfo], host: String, port: Option[Int]) =>
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
}

object UrlParser {
  def parseUserInfo(s: String)(implicit config: UriConfig = UriConfig.default): UserInfo = ???

  def parseUrn(s: String)(implicit config: UriConfig = UriConfig.default): Urn = ???

  def parseUrnPath(s: String)(implicit config: UriConfig = UriConfig.default): UrnPath = ???

  def parseUrlPath(s: String)(implicit config: UriConfig = UriConfig.default): UrlPath = ???

  def parseUrlWithoutAuthority(s: String)(implicit config: UriConfig = UriConfig.default): UrlWithoutAuthority = ???

  def parseAbsoluteUrl(s: String)(implicit config: UriConfig = UriConfig.default): AbsoluteUrl = ???

  def parseProtocolRelativeUrl(s: String)(implicit config: UriConfig = UriConfig.default): ProtocolRelativeUrl = ???

  def parseUrlWithAuthority(s: String)(implicit config: UriConfig = UriConfig.default): UrlWithAuthority = ???

  def parseRelativeUrl(s: String)(implicit config: UriConfig = UriConfig.default): RelativeUrl = ???

  def parseUri(s: String)(implicit config: UriConfig = UriConfig.default): Uri = ???

  def parsePath(s: String)(implicit config: UriConfig = UriConfig.default): UrlPath = ???

  def parseAuthority(s: String)(implicit config: UriConfig = UriConfig.default): Authority = ???

  def parseUrl(s: String)(implicit config: UriConfig): Url = {
    val parser = new UrlParser(s)

    parser._url.run() match {
      case Success(uri) =>
        uri

      case Failure(pe @ ParseError(position, _, formatTraces)) =>
        throw new java.net.URISyntaxException(s, "Invalid URI could not be parsed. " + formatTraces, position.index)

      case Failure(e) =>
        throw e
    }
  }

  def parseQuery(s: String)(implicit config: UriConfig) = {
    val withQuestionMark = if(s.head == '?') s else "?" + s
    val parser = new UrlParser(withQuestionMark)

    parser._queryString.run() match {
      case Success(queryString) =>
        queryString

      case Failure(pe @ ParseError(position, _, formatTraces)) =>
        throw new java.net.URISyntaxException(s, "Invalid URI could not be parsed. " + formatTraces, position.index)

      case Failure(e) =>
        throw e
    }
  }

  def parseQueryParam(s: String)(implicit config: UriConfig): (String, Option[String]) = {
    val parser = new UrlParser(s)

    parser._queryParamOrTok.run() match {
      case Success(queryParam) =>
        queryParam

      case Failure(pe @ ParseError(position, _, formatTraces)) =>
        throw new java.net.URISyntaxException(s, "Invalid URI could not be parsed. " + formatTraces, position.index)

      case Failure(e) =>
        throw e
    }
  }
}
