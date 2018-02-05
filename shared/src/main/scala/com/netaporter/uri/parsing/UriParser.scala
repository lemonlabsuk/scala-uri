package com.netaporter.uri.parsing

import com.netaporter.uri.config.UriConfig

import scala.util.Failure
import org.parboiled2._
import com.netaporter.uri.{PathPart, QueryString, StringPathPart}
import com.netaporter.uri.decoding.UriDecoder
import com.netaporter.uri.Parameters._
import com.netaporter.uri.inet.PublicSuffixes
import org.parboiled2.ParseError

import scala.util.Success
import scala.util.Failure

trait UriParser {

  def pathDecoder: UriDecoder
  def queryDecoder: UriDecoder
  def fragmentDecoder: UriDecoder

  def _pathSegment: Rule1[PathPart]

  val extractInt = (num: String) =>
    num.toInt

  val extractUserInfo = (user: String, pass: Option[Option[String]]) =>
    UserInfo(pathDecoder.decode(user), pass.map(_.fold("")(pathDecoder.decode)))

  val extractAuthority = (userInfo: Option[UserInfo], host: String, port: Option[String]) =>
    Authority(userInfo.map(_.user), userInfo.flatMap(_.pass), host, port.map(_.toInt))

  val extractFragment = (x: String) =>
    fragmentDecoder.decode(x)

  val extractQueryString = (tuples: ParamSeq) =>
    QueryString(tuples.toVector.map(queryDecoder.decodeTuple))

  val extractPathPart = (pathPart: String) => {
    val decodedPathPart = pathDecoder.decode(pathPart)
    StringPathPart(decodedPathPart)
  }

  val extractAbsPath = (pp: Seq[PathPart]) =>
    Path(startsWithSlash = true, pp.toVector)

  val extractRelPath = (maybeSlash: String, pp: Seq[PathPart]) =>
    Path(maybeSlash.nonEmpty, pp.toVector)

  val extractTuple = (k: String, v: String) =>
    k -> Some(v)

  val extractTok = (k: String) => k -> Option.empty[String]

  /**
   * Used to made parsing easier to follow
   */
  case class Authority(user: Option[String], password: Option[String], host: String, port: Option[Int])
  case class UserInfo(user: String, pass: Option[String])
  case class Path(startsWithSlash: Boolean, pathParts: Vector[PathPart])
}

object UriParser {
  def parse(s: String, config: UriConfig) = {
    val parser = new DefaultUriParser(s, config)

    parser._uri.run() match {
      case Success(uri) =>
        uri

      case Failure(pe @ ParseError(position, _, formatTraces)) =>
        throw new java.net.URISyntaxException(s, "Invalid URI could not be parsed. " + formatTraces, position.index)

      case Failure(e) =>
        throw e
    }
  }

  def parseQuery(s: String, config: UriConfig) = {
    val withQuestionMark = if(s.head == '?') s else "?" + s
    val parser = new DefaultUriParser(withQuestionMark, config)

    parser._queryString.run() match {
      case Success(queryString) =>
        queryString

      case Failure(pe @ ParseError(position, _, formatTraces)) =>
        throw new java.net.URISyntaxException(s, "Invalid URI could not be parsed. " + formatTraces, position.index)

      case Failure(e) =>
        throw e
    }
  }

  def parseQueryParam(s: String, config: UriConfig): Param = {
    val parser = new DefaultUriParser(s, config)

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
