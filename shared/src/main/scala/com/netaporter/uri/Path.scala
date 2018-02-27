package com.netaporter.uri

import com.netaporter.uri.config.UriConfig
import com.netaporter.uri.parsing.{UrlParser, UrnParser}

import scala.collection.GenTraversableOnce

sealed trait Path {
  def config: UriConfig
  def parts: Vector[String]
  private[uri] def toString(config: UriConfig): String

  /**
    * Returns the path with no encoders taking place (e.g. non ASCII characters will not be percent encoded)
    * @return String containing the raw path for this Uri
    */
  def toStringRaw: String =
    toString(config.withNoEncoding)

  override def toString: String =
    toString(config)
}

object Path {
  def parse(s: CharSequence)(implicit config: UriConfig = UriConfig.default): Path =
    UrlParser.parsePath(s.toString)
}

sealed trait UrlPath extends Path {
  def withParts(parts: GenTraversableOnce[String]): UrlPath

  def toRootless: RootlessPath
  def toAbsolute: AbsolutePath
  def toAbsoluteOrEmpty: AbsoluteOrEmptyPath

  def isEmpty: Boolean

  def addPart(part: String): UrlPath =
    withParts(parts :+ part)

  def addParts(otherParts: String*): UrlPath =
    addParts(otherParts)

  def addParts(otherParts: GenTraversableOnce[String]): UrlPath =
    withParts(parts = parts ++ otherParts)

  /**
    * Returns the encoded path. By default non ASCII characters in the path are percent encoded.
    * @return String containing the path for this Uri
    */
  private[uri] def toString(c: UriConfig): String = {
    val encodedParts = parts.map(p => c.pathEncoder.encode(p, c.charset))
    encodedParts.mkString("/")
  }
}

object UrlPath {
  def empty: UrlPath = EmptyPath
  val slash: UrlPath = AbsolutePath(Vector.empty)

  def apply(parts: GenTraversableOnce[String]): UrlPath =
    if(parts.isEmpty) EmptyPath
    else AbsolutePath(parts.toVector)

  def parse(s: CharSequence)(implicit config: UriConfig = UriConfig.default): UrlPath =
    UrlParser.parsePath(s.toString)
}

/**
  * This trait has two subclasses; `AbsolutePath` and `EmptyPath`.
  * This encompasses the paths allowed to be used in URLs that have an Authority. As per RFC 3986:
  *
  *   When authority is present, the path must either be empty or begin with a slash ("/") character.
  */
trait AbsoluteOrEmptyPath extends UrlPath {

  def toAbsoluteOrEmpty: AbsoluteOrEmptyPath =
    this

  def toRootless: RootlessPath =
    RootlessPath(parts)
}

object EmptyPath extends AbsoluteOrEmptyPath {

  def isEmpty: Boolean =
    true

  def toAbsolute: AbsolutePath =
    AbsolutePath(Vector.empty)

  def withParts(parts: GenTraversableOnce[String]): UrlPath =
    UrlPath(parts.toVector)

  def config: UriConfig =
    UriConfig.default

  def parts: Vector[String] =
    Vector.empty
}

final case class RootlessPath(parts: Vector[String])(implicit val config: UriConfig = UriConfig.default) extends UrlPath {

  def toRootless: RootlessPath =
    this

  def toAbsolute: AbsolutePath =
    AbsolutePath(parts)

  def toAbsoluteOrEmpty: AbsoluteOrEmptyPath =
    if(parts.isEmpty) EmptyPath
    else AbsolutePath(parts)

  def withParts(otherParts: GenTraversableOnce[String]): UrlPath =
    copy(parts = otherParts.toVector)

  /**
    * Returns true if this path is empty (i.e. calling `toString` will return an empty String)
    * An empty RootlessPath is the same as EmptyPath. Use EmptyPath instead wherever possible.
    */
  def isEmpty: Boolean =
    parts.isEmpty
}

/**
  * An AbsolutePath is a path that starts with a slash
  *
  * @param parts
  */
final case class AbsolutePath(parts: Vector[String])(implicit val config: UriConfig = UriConfig.default) extends AbsoluteOrEmptyPath {

  def toAbsolute: AbsolutePath =
    this

  def withParts(otherParts: GenTraversableOnce[String]): UrlPath =
    copy(parts = otherParts.toVector)

  /**
    * Always returns false as we always have at least a leading slash
    */
  def isEmpty: Boolean =
    false

  override private[uri] def toString(c: UriConfig): String =
    "/" + super.toString(c)
}


final case class UrnPath(nid: String, nss: String)(implicit val config: UriConfig = UriConfig.default) extends Path {

  def parts: Vector[String] =
    Vector(nid, nss)

  def toUrlPath: UrlPath =
    UrlPath(parts)

  private[uri] def toString(c: UriConfig): String =
    nid + ":" + c.pathEncoder.encode(nss, c.charset)
}

object UrnPath {
  def parse(s: CharSequence)(implicit config: UriConfig = UriConfig.default): UrnPath =
    UrnParser.parseUrnPath(s.toString)
}
