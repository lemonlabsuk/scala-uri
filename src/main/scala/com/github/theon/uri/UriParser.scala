package com.github.theon.uri

import org.parboiled.scala._
import org.parboiled.errors.{ErrorUtils, ParsingException}
import scala.collection.immutable.ListMap

object UriParser extends Parser {

  def scheme = rule { oneOrMore(alphaNumeric) ~> extract ~ "://" }

  def alphaNumeric = rule { "0" - "9" | "a" - "z" | "A" - "Z" }

  def hostname = rule { oneOrMore(!anyOf(":/") ~ ANY) ~> extract }

  def port = rule { ":" ~ (oneOrMore("0" - "9") ~> (_.toInt)) }

  def pathSegment = rule { zeroOrMore(!anyOf("/?") ~ ANY) ~> extract }

  def queryKeyValue = group(zeroOrMore(!anyOf("=&") ~ ANY) ~> extract ~ "=" ~ zeroOrMore(!anyOf("=&") ~ ANY) ~> extract)

  def queryString = optional("?") ~ zeroOrMore(queryKeyValue, separator = "&") ~~> (tuples => tuplesToQuerystring(tuples))

  /**
   * Anyone have a cleaner way extract strings?
   */
  def extract = (x: String) => x

  lazy val uri: Rule1[Uri] = rule {
    optional(scheme ~ hostname) ~
      optional(port) ~
      optional("/") ~
      zeroOrMore(pathSegment, separator = "/") ~
      queryString ~~> ((schemeHost, p, pp, qs) => new Uri(schemeHost.map(_._1), schemeHost.map(_._2), p, pp, qs))
  }

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
