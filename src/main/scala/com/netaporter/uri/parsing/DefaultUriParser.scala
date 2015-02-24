package com.netaporter.uri.parsing

import org.parboiled2._
import com.netaporter.uri._
import com.netaporter.uri.config.UriConfig
import Parameters._

class DefaultUriParser(val input: ParserInput, conf: UriConfig) extends Parser with UriParser {

  def _scheme: Rule1[String] = rule {
    capture(oneOrMore(CharPredicate.AlphaNum))
  }

  def _host_name: Rule1[String] = rule {
    capture(oneOrMore(!anyOf(":/?") ~ ANY))
  }

  def _userInfo: Rule1[UserInfo] = rule {
    capture(oneOrMore(!anyOf(":@") ~ ANY)) ~ optional(":" ~ capture(oneOrMore(!anyOf("@") ~ ANY))) ~ "@" ~> extractUserInfo
  }

  //TODO Try harder to make this a Rule1[Int] using ~> extractInt
  def _port: Rule1[String] = rule {
    ":" ~ capture(oneOrMore(CharPredicate.Digit))
  }

  def _authority: Rule1[Authority] = rule {
    ((optional(_userInfo) ~ _host_name ~ optional(_port)) | (push[Option[UserInfo]](None) ~ _host_name ~ optional(_port))) ~> extractAuthority
  }

  def _pathSegment: Rule1[PathPart] = rule {
    capture(zeroOrMore(!anyOf("/?#") ~ ANY)) ~> extractPathPart
  }

  /**
   * A sequence of path parts that MUST start with a slash
   */
  def _abs_path: Rule1[Vector[PathPart]] = rule {
    zeroOrMore("/" ~ _pathSegment) ~> extractPathParts
  }

  /**
   * A sequence of path parts optionally starting with a slash
   */
  def _rel_path: Rule1[Vector[PathPart]] = rule {
    optional("/") ~ zeroOrMore(_pathSegment).separatedBy("/") ~> extractPathParts
  }

  def _queryParam: Rule1[Param] = rule {
    capture(zeroOrMore(!anyOf("=&#") ~ ANY)) ~ "=" ~ capture(zeroOrMore(!anyOf("&#") ~ ANY)) ~> extractTuple
  }

  def _queryTok: Rule1[Param] = rule {
    capture(zeroOrMore(!anyOf("=&#") ~ ANY)) ~> extractTok
  }

  def _queryString: Rule1[QueryString] = rule {
    "?" ~ zeroOrMore(_queryParam | _queryTok).separatedBy("&") ~> extractQueryString
  }

  def _fragment: Rule1[String] = rule {
    "#" ~ capture(zeroOrMore(!anyOf("#") ~ ANY)) ~> extractFragment
  }

  def _abs_uri: Rule1[Uri] = rule {
    _scheme ~ "://" ~ optional(_authority) ~ _abs_path ~ optional(_queryString) ~ optional(_fragment) ~> extractAbsUri
  }

  def _protocol_rel_uri: Rule1[Uri] = rule {
    "//" ~ optional(_authority) ~ _abs_path ~ optional(_queryString) ~ optional(_fragment) ~> extractProtocolRelUri
  }

  def _rel_uri: Rule1[Uri] = rule {
    _rel_path ~ optional(_queryString) ~ optional(_fragment) ~> extractRelUri
  }

  def _uri: Rule1[Uri] = rule {
    (_abs_uri | _protocol_rel_uri | _rel_uri) ~ EOI
  }

  val extractAbsUri = (scheme: String, authority: Option[Authority], pp: Seq[PathPart], qs: Option[QueryString], f: Option[String]) =>
    extractUri (
      scheme = Some(scheme),
      authority = authority,
      pathParts = pp,
      query = qs,
      fragment = f
    )

  val extractProtocolRelUri = (authority: Option[Authority], pp: Seq[PathPart], qs: Option[QueryString], f: Option[String]) =>
    extractUri (
      authority = authority,
      pathParts = pp,
      query = qs,
      fragment = f
    )

  val extractRelUri = (pp: Seq[PathPart], qs: Option[QueryString], f: Option[String]) =>
    extractUri (
      pathParts = pp,
      query = qs,
      fragment = f
    )

  def extractUri(scheme: Option[String] = None,
                 authority: Option[Authority] = None,
                 pathParts: Seq[PathPart],
                 query: Option[QueryString],
                 fragment: Option[String]) =
    new Uri(
      scheme = scheme,
      user = authority.flatMap(_.user),
      password = authority.flatMap(_.password),
      host = authority.map(_.host),
      port = authority.flatMap(_.port),
      pathParts = pathParts,
      query = query.getOrElse(EmptyQueryString),
      fragment = fragment
    )

  def pathDecoder = conf.pathDecoder
  def queryDecoder = conf.queryDecoder
  def fragmentDecoder = conf.fragmentDecoder
}