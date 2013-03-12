package com.github.theon.uri

import util.parsing.combinator.RegexParsers

class UriParser extends RegexParsers {

  val relScheme =  "://" ^^ { x => None }

  val absScheme =  "[a-zA-Z0-9]+".r <~ "://" ^^ { Some(_) }

  val scheme = absScheme | relScheme

  val hostname =  "[^:/]+".r

  val port = "[0-9]+".r ^^ { _.toInt }

  val pathSegment = "[^/\\?]*".r

  val queryKeyValue = "[^=&]+".r ~ "=" ~ "[^=&]+".r ^^ {
    case key ~ equals ~ value => (key, value)
  }

  val uri = (scheme ~ hostname).? ~ (":" ~> port).? ~ repsep(pathSegment, "/") ~ "?".? ~ repsep(queryKeyValue, "&") ^^ {
    case schemeHost ~ port ~ pathSegments ~ question ~ queryPairs => {
      new Uri(schemeHost.flatMap(_._1), schemeHost.map(_._2), port, pathSegments, tuplesToQuerystring(queryPairs))
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
  /**
   * When thread safety issues mentioned in https://issues.scala-lang.org/browse/SI-4929 are fixed,
   * we won't need to make a new UriParser for every parse, we can reuse a single UriParser for better
   * performance.
   * I was going to make reuse of a single UriParser for 2.10 (as the thread safety issues don't exist in 2.10),
   * however there is talk of reintroducing these issues to avoid a memory leak... (so I will just keep and eye on the
   * SI-4929 ticket for the meantime)
   */
  def parse(s: CharSequence) = {
    val parser = new UriParser()
    val res = parser.parseAll(parser.uri, s).get
    cleanup
    res
  }

  /**
   * Cleanup to prevent memory leak mentioned in ticket https://issues.scala-lang.org/browse/SI-4929
   */
  def cleanup = {
    try {
      val field = getClass.getDeclaredField("scala$util$parsing$combinator$Parsers$$lastNoSuccessVar")
      field.setAccessible(true)
      val field2 = classOf[scala.util.DynamicVariable[_]].getDeclaredField("tl")
      field2.setAccessible(true)
      field2.get(field.get(this)).asInstanceOf[java.lang.ThreadLocal[_]].remove()
      field.set(this, null)
    } catch {
      case e:NoSuchFieldException => //2.9.2 - no clean up required
    }
  }
}
