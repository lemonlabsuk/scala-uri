package com.github.theon.uri

import org.parboiled.scala._
import org.parboiled.errors.{ErrorUtils, ParsingException}

object UriParser extends Parser {

  def _scheme = rule { oneOrMore(_alphaNumeric) ~> extract }

  def _alphaNumeric = rule { "0" - "9" | "a" - "z" | "A" - "Z" }

  def _hostname = rule { oneOrMore(!anyOf(":/?") ~ ANY) ~> extract }

  def _userInfo = (oneOrMore(!anyOf(":@") ~ ANY) ~> extract) ~ optional(":" ~ (oneOrMore(!anyOf("@") ~ ANY) ~> extract)) ~ "@"

  def _port = rule { oneOrMore("0" - "9") ~> extract }

  def _authority = (optional(_userInfo) ~ _hostname ~ optional(":" ~ _port)) ~~>  ((ui, h, p) => Authority(ui.map(_._1), ui.flatMap(_._2), h, p.map(_.toInt)))

  def _pathSegment = rule { zeroOrMore(!anyOf("/?#") ~ ANY) ~> extract }

  def _path = zeroOrMore(_pathSegment, separator = "/")

  def _queryKeyValue = group(zeroOrMore(!anyOf("=&#") ~ ANY) ~> extract ~ "=" ~ zeroOrMore(!anyOf("=&#") ~ ANY) ~> extract)

  def _queryString = optional("?") ~ zeroOrMore(_queryKeyValue, separator = "&") ~~> (tuples => tuplesToQuerystring(tuples))

  def _fragment = rule { "#" ~ (zeroOrMore(!anyOf("#") ~ ANY) ~> extract) }

  /**
   * Anyone have a cleaner way extract strings?
   */
  def extract = (x: String) => x

  lazy val uri: Rule1[Uri] = rule {
    optional(optional(_scheme ~ ":") ~ "//" ~ _authority) ~ optional("/") ~ _path ~ _queryString ~ optional(_fragment) ~~> {
      (sa, pp, qs, f) => {
        val scheme = sa.map(_._1)
        val authority = sa.map(_._2)

        new Uri(
          protocol = scheme.flatMap(x => x), //TODO: Change to scheme.flatten when 2.9.2 support is dropped
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

  /**
   * Used to made parsing easier to follow
   */
  protected  case class Authority(user: Option[String], password: Option[String], host: String, port: Option[Int])

  def tuplesToQuerystring(tuples: List[(String,String)]) = {
    val map = tuples.groupBy(_._1).map(kv => {
      val (k,v) = kv
      (k,v.map(_._2))
    })

    Querystring(map)
  }

  def parse(s: String, decoder: UriDecoder) = {
    val parsingResult = ReportingParseRunner(uri).run(s)
    parsingResult.result match {
      case Some(uri) => decoder.decode(uri)
      case None => throw new ParsingException("Invalid Uri:\n" + ErrorUtils.printParseErrors(parsingResult))
    }
  }
}
