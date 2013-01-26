package com.github.theon.uri

import util.parsing.combinator.RegexParsers

class UriParser extends RegexParsers {

  val protocol =  "[a-zA-Z0-9]+".r <~ "://"

  val hostname =  "[^:/]+".r

  val port = "[0-9]+".r ^^ { _.toInt }

  val pathSegment = "[^/\\?]*".r

  val queryKeyValue = "[^=&]+".r ~ "=" ~ "[^=&]+".r ^^ {
    case key ~ equals ~ value => (key, value)
  }

  val uri = (protocol ~ hostname).? ~ (":" ~> port).? ~ repsep(pathSegment, "/") ~ "?".? ~ repsep(queryKeyValue, "&") ^^ {
    case protocolHost ~ port ~ pathSegments ~ question ~ queryPairs => {
      new Uri(protocolHost.map(_._1), protocolHost.map(_._2), port, pathSegments, tuplesToQuerystring(queryPairs))
    }
  }

  def tuplesToQuerystring(tuples:List[(String,String)]) = {
    val map = tuples.groupBy(_._1).map(kv => {
      val (k,v) = kv
      (k,v.map(_._2))
    })

    Querystring(map)
  }
}
object UriParser {
  val parser = new UriParser()
  def parse(s: CharSequence) = parser.parseAll(parser.uri, s).get
}