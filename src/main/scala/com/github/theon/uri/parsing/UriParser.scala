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

  def _authority = rule { (optional(_userInfo) ~ _hostname ~ optional(":" ~ _port)) ~~>  ((ui, h, p) => Authority(ui.map(_._1), ui.flatMap(_._2), h, p.map(_.toInt))) }

  def _matrixParam = rule { group(zeroOrMore(!anyOf(";/=?#") ~ ANY) ~> extract ~ "=" ~ zeroOrMore(!anyOf(";/=?#") ~ ANY) ~> extract) }

  def _plainPathPart = rule { zeroOrMore(!anyOf(";/?#") ~ ANY) ~> extract }

  def _pathSegment = rule { _plainPathPart ~ optional(";") ~ zeroOrMore(_matrixParam, separator = ";") }

  def _path = rule { zeroOrMore(_pathSegment, separator = "/") ~~> (pp => toPathParts(pp)) }

  def _queryParam = rule { group(zeroOrMore(!anyOf("=&#") ~ ANY) ~> extract ~ "=" ~ zeroOrMore(!anyOf("=&#") ~ ANY) ~> extract) }

  def _queryString = rule { optional("?") ~ zeroOrMore(_queryParam, separator = "&") ~~> (qs => toQueryString(qs)) }

  def _fragment = rule { "#" ~ (zeroOrMore(!anyOf("#") ~ ANY) ~> extract) }

  lazy val _uri: Rule1[Uri] = rule {
    optional(optional(_scheme ~ ":") ~ "//" ~ _authority) ~ optional("/") ~ _path ~ _queryString ~ optional(_fragment) ~~> {
      (sa, pp, qs, f) => {
        val scheme = sa.map(_._1)
        val authority = sa.map(_._2)

        new Uri(
          scheme = scheme.flatten,
          user = authority.flatMap(_.user),
          password = authority.flatMap(_.password),
          host = authority.map(_.host),
          port = authority.flatMap(_.port),
          pathParts = pp,
          query = qs,
          fragment = f
       )
     }
    }
  }

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
      //Nicer way to do this?
      case e: ParserRuntimeException if e.getCause.isInstanceOf[UriDecodeException] => throw e.getCause
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
