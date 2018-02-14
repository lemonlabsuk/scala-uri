package com.netaporter.uri.parsing

import java.net.URISyntaxException

import org.parboiled2._
import com.netaporter.uri._
import com.netaporter.uri.config.UriConfig
import Parameters._
import com.netaporter.uri.inet.PublicSuffixes

class DefaultUriParser(val input: ParserInput, conf: UriConfig) extends Parser with UriParser {

  def _scheme: Rule1[String] = rule {
    capture(CharPredicate.Alpha ~ zeroOrMore(CharPredicate.AlphaNum | anyOf("+-.")))
  }

  def _host_name: Rule1[String] = rule {
    capture("[" ~ oneOrMore(!anyOf("[]/?#") ~ ANY) ~ "]") | capture(oneOrMore(!anyOf(":/?#") ~ ANY))
  }

  def _userInfo: Rule1[UserInfo] = rule {
    capture(oneOrMore(!anyOf(":/?@") ~ ANY)) ~ optional(":" ~ optional(capture(oneOrMore(!anyOf("@") ~ ANY)))) ~ "@" ~> extractUserInfo
  }

  //TODO Try harder to make this a Rule1[Int] using ~> extractInt
  def _port: Rule1[String] = rule {
    ":" ~ capture(oneOrMore(CharPredicate.Digit))
  }

  def _authority: Rule1[Authority] = rule {
    "//" ~ ((optional(_userInfo) ~ _host_name ~ optional(_port)) | (push[Option[UserInfo]](None) ~ _host_name ~ optional(_port))) ~> extractAuthority
  }

  def _authorityAsOpt: Rule1[Option[Authority]] = rule {
    _authority ~> ((a: Authority) => Some(a))
  }

  def _pathSegment: Rule1[PathPart] = rule {
    capture(zeroOrMore(!anyOf("/?#") ~ ANY)) ~> extractPathPart
  }

  /**
    * A sequence of path parts that MUST start with a slash
    *
    * If a URI contains an authority component, then the path component must either be empty
    * or begin with a slash ("/") character.
    */
  def _pathForAuthority: Rule1[Path] = rule {
    zeroOrMore("/" ~ _pathSegment) ~> extractAbsPath
  }

  def _authorityWithPath: Rule2[Option[Authority], Path] = rule {
    _authorityAsOpt ~ _pathForAuthority
  }

  def _noAuthorityWithPath: Rule2[Option[Authority], Path] = rule {
    push[Option[Authority]](None) ~ _relPath
  }

  /**
   * A sequence of path parts optionally starting with a slash
   */
  def _relPath: Rule1[Path] = rule {
    capture(optional("/")) ~ zeroOrMore(_pathSegment).separatedBy("/") ~> extractRelPath
  }

  def _queryParam: Rule1[Param] = rule {
    capture(zeroOrMore(!anyOf("=&#") ~ ANY)) ~ "=" ~ capture(zeroOrMore(!anyOf("&#") ~ ANY)) ~> extractTuple
  }

  def _queryTok: Rule1[Param] = rule {
    capture(zeroOrMore(!anyOf("=&#") ~ ANY)) ~> extractTok
  }

  def _queryParamOrTok: Rule1[Param] = rule {
    _queryParam | _queryTok
  }

  def _queryString: Rule1[QueryString] = rule {
    "?" ~ zeroOrMore(_queryParamOrTok).separatedBy("&") ~> extractQueryString
  }

  def _fragment: Rule1[String] = rule {
    "#" ~ capture(zeroOrMore(ANY)) ~> extractFragment
  }

  def _abs_uri: Rule1[Uri] = rule {
    _scheme ~ ":" ~ (_authorityWithPath | _noAuthorityWithPath) ~ optional(_queryString) ~ optional(_fragment) ~> extractAbsUri
  }

  def _protocol_rel_uri: Rule1[Uri] = rule {
    _authority ~ _pathForAuthority ~ optional(_queryString) ~ optional(_fragment) ~> extractProtocolRelUri
  }

  def _rel_uri: Rule1[Uri] = rule {
    _relPath ~ optional(_queryString) ~ optional(_fragment) ~> extractRelUri
  }

  def _uri: Rule1[Uri] = rule {
    (_abs_uri | _protocol_rel_uri | _rel_uri) ~ EOI
  }

  val extractAbsUri = (scheme: String, authority: Option[Authority], path: Path, qs: Option[QueryString], f: Option[String]) =>
    extractUri (
      scheme = Some(scheme),
      authority = authority,
      path = path,
      query = qs,
      fragment = f
    )

  val extractProtocolRelUri = (authority: Authority, path: Path, qs: Option[QueryString], f: Option[String]) =>
    extractUri (
      authority = Some(authority),
      path = path,
      query = qs,
      fragment = f
    )

  val extractRelUri = (path: Path, qs: Option[QueryString], f: Option[String]) =>
    extractUri (
      path = path,
      query = qs,
      fragment = f
    )

  def extractUri(scheme: Option[String] = None,
                 authority: Option[Authority] = None,
                 path: Path,
                 query: Option[QueryString],
                 fragment: Option[String]) =
    new Uri(
      scheme = scheme,
      user = authority.flatMap(_.user),
      password = authority.flatMap(_.password),
      host = authority.map(_.host),
      port = authority.flatMap(_.port),
      pathParts = path.pathParts,
      pathStartsWithSlash = path.startsWithSlash,
      query = query.getOrElse(EmptyQueryString),
      fragment = fragment
    )

  def pathDecoder = conf.pathDecoder
  def queryDecoder = conf.queryDecoder
  def fragmentDecoder = conf.fragmentDecoder
}
