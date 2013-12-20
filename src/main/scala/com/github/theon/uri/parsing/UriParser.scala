package com.github.theon.uri.parsing

import org.parboiled.scala._
import org.parboiled.errors.{ParserRuntimeException, ErrorUtils, ParsingException}
import com.github.theon.uri.decoding.{UriDecodeException, UriDecoder}
import com.github.theon.uri._
import com.github.theon.uri.config.UriConfig
import com.github.theon.uri.QueryString
import scala.Some
import Parameters._

object UriParser extends Parser {

  def _scheme = rule { oneOrMore(_alphaNumeric) ~> extract }

  def _alphaNumeric = rule { "0" - "9" | "a" - "z" | "A" - "Z" }

  def _hostname = rule { oneOrMore(!anyOf(":/?") ~ ANY) ~> extract }

  def _userInfo = rule { (oneOrMore(!anyOf(":@") ~ ANY) ~> extract) ~ optional(":" ~ (oneOrMore(!anyOf("@") ~ ANY) ~> extract)) ~ "@" }

  def _port = rule { oneOrMore("0" - "9") ~> extract }

  def _authority = rule { optional((optional(_userInfo) ~ _hostname ~ optional(":" ~ _port)) ~~> ((ui, h, p) => Authority(ui.map(_._1), ui.flatMap(_._2), h, p.map(_.toInt)))) }

  def _matrixParam = rule { group(zeroOrMore(!anyOf(";/=?#") ~ ANY) ~> extract ~ "=" ~ zeroOrMore(!anyOf(";/=?#") ~ ANY) ~> extract) }

  def _plainPathPart = rule { zeroOrMore(!anyOf(";/?#") ~ ANY) ~> extract }

  def _pathSegment = rule { _plainPathPart ~ optional(";") ~ zeroOrMore(_matrixParam, separator = ";") }

  /**
   * A sequence of path parts that MUST start with a slash
   */
  def _abs_path = rule { zeroOrMore("/" ~ _pathSegment) ~~> (pp => toPathParts(pp)) }

  /**
   * A sequence of path parts optionally starting with a slash
   */
  def _rel_path = rule { optional("/") ~ zeroOrMore(_pathSegment, separator = "/") ~~> (pp => toPathParts(pp)) }

  def _queryParam = rule { group(zeroOrMore(!anyOf("=&#") ~ ANY) ~> extract ~ "=" ~ zeroOrMore(!anyOf("=&#") ~ ANY) ~> extract) }

  def _queryString = rule { optional("?") ~ zeroOrMore(_queryParam, separator = "&") ~~> (qs => toQueryString(qs)) }

  def _fragment = rule { "#" ~ (zeroOrMore(!anyOf("#") ~ ANY) ~> extract) }

  def _abs_uri: Rule1[Uri] = rule {
    _scheme ~ "://" ~ _authority ~ _abs_path ~ _queryString ~ optional(_fragment) ~~> extractAbsUri
  }

  def _protocol_rel_uri: Rule1[Uri] = rule {
    "//" ~ _authority ~ _abs_path ~ _queryString ~ optional(_fragment) ~~> extractProtocolRelUri
  }

  def _rel_uri: Rule1[Uri] = rule {
    _rel_path ~ _queryString ~ optional(_fragment) ~~> extractRelUri
  }

  lazy val _uri: Rule1[Uri] = rule {
    _abs_uri | _protocol_rel_uri | _rel_uri
  }

  def extractAbsUri(scheme: String, authority: Option[Authority], pp: Seq[PathPart], qs: QueryString, f: Option[String]) =
    extractUri (
      scheme = Some(scheme),
      authority = authority,
      pathParts = pp,
      query = qs,
      fragment = f
    )

  def extractProtocolRelUri(authority: Option[Authority], pp: Seq[PathPart], qs: QueryString, f: Option[String]) =
    extractUri (
      authority = authority,
      pathParts = pp,
      query = qs,
      fragment = f
    )

  def extractRelUri(pp: Seq[PathPart], qs: QueryString, f: Option[String]) =
    extractUri (
      pathParts = pp,
      query = qs,
      fragment = f
    )

  def extractUri(scheme: Option[String] = None,
                 authority: Option[Authority] = None,
                 pathParts: Seq[PathPart],
                 query: QueryString,
                 fragment: Option[String]) =
    new Uri(
      scheme = scheme,
      user = authority.flatMap(_.user),
      password = authority.flatMap(_.password),
      host = authority.map(_.host),
      port = authority.flatMap(_.port),
      pathParts = pathParts,
      query = query,
      fragment = fragment
    )

  def extract = (x: String) => x

  /**
   * Used to made parsing easier to follow
   */
  case class Authority(user: Option[String], password: Option[String], host: String, port: Option[Int])

  def toQueryString(tuples: ParamSeq) =
    QueryString(tuples.toVector)

  def toPathParts(pathParts: List[(String,ParamSeq)]) = {
    val pp = pathParts.map(pp => {
      val (plain, matrixParams) = pp
      PathPart(plain, matrixParams.toVector)
    })
    pp.toVector
  }

  def parse(s: String, config: UriConfig) = {
    try {
      val parsingResult = ReportingParseRunner(_uri).run(s)

      parsingResult.result match {
        case Some(uri) => decode(uri, config)
        case None => throw new ParsingException("Invalid Uri:\n" + ErrorUtils.printParseErrors(parsingResult))
      }
    } catch {
      case e: ParserRuntimeException => e.getCause match {
        case ude: UriDecodeException => throw ude
        case _ => throw e
      }
    }
  }
  
  def decode(uri: Uri, config: UriConfig) = {
    val pathDecoder = config.pathDecoder
    val pathParts = uri.pathParts.map {
      case p: StringPathPart => p.map(pathDecoder.decode)
      case p: MatrixParams => p.map(pathDecoder.decode).mapParams(pathDecoder.decodeTuple)
    }
    val query = uri.query.mapParams(config.queryDecoder.decodeTuple)
    val fragment = uri.fragment.map(config.fragmentDecoder.decode)

    uri.copy(pathParts = pathParts, query = query, fragment = fragment)
  }
}
