package io.lemonlabs.uri
import cats.{Eq, Order, Show}
import io.lemonlabs.uri.config.UriConfig
import io.lemonlabs.uri.parsing.{UrlParser, UrnParser}
import io.lemonlabs.uri.typesafe.{PathPart, TraversablePathParts}

import io.lemonlabs.uri.typesafe.PathPart.ops._
import io.lemonlabs.uri.typesafe.TraversablePathParts.ops._

import scala.util.Try

sealed trait Path extends Product with Serializable {
  def config: UriConfig
  def parts: Vector[String]
  private[uri] def toString(config: UriConfig): String

  def isEmpty: Boolean
  def nonEmpty: Boolean = !isEmpty

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
  def parseTry(s: CharSequence)(implicit config: UriConfig = UriConfig.default): Try[Path] =
    UrlParser.parsePath(s.toString)

  def parseOption(s: CharSequence)(implicit config: UriConfig = UriConfig.default): Option[Path] =
    parseTry(s).toOption

  def parse(s: CharSequence)(implicit config: UriConfig = UriConfig.default): Path =
    parseTry(s).get

  def unapply(path: Path): Option[Vector[String]] =
    Some(path.parts)

  implicit val eqPath: Eq[Path] = Eq.fromUniversalEquals
  implicit val showPath: Show[Path] = Show.fromToString
  implicit val orderPath: Order[Path] = Order.by(_.toString())
}

object PathParts {
  def unapplySeq(path: Path): Option[Seq[String]] =
    Some(path.parts)
}

sealed trait UrlPath extends Path {
  def withParts(parts: Iterable[String]): UrlPath

  def toRootless: RootlessPath
  def toAbsolute: AbsolutePath
  def toAbsoluteOrEmpty: AbsoluteOrEmptyPath

  def addPart[P: PathPart](part: P): UrlPath =
    withParts(parts :+ part.path)

  def addParts[P: TraversablePathParts](otherParts: P): UrlPath =
    withParts(parts = parts ++ otherParts.toSeq)

  def addParts[P: PathPart](first: P, second: P, otherParts: P*): UrlPath =
    addParts((Vector(first, second) ++ otherParts).map(_.path))

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

  def apply(parts: Iterable[String]): UrlPath =
    if (parts.isEmpty) EmptyPath
    else AbsolutePath(parts.toVector)

  def parseTry(s: CharSequence)(implicit config: UriConfig = UriConfig.default): Try[UrlPath] =
    UrlParser.parsePath(s.toString)

  def parseOption(s: CharSequence)(implicit config: UriConfig = UriConfig.default): Option[UrlPath] =
    parseTry(s).toOption

  def parse(s: CharSequence)(implicit config: UriConfig = UriConfig.default): UrlPath =
    parseTry(s).get

  /**
    * Unlike `UrlPath.parse`, this method treats the supplied String as a raw path and does not
    * require reserved characters to be PercentEncoded
    */
  def fromRaw(s: String)(implicit config: UriConfig = UriConfig.default): UrlPath = {
    def parts = s.split('/').toVector
    s.headOption match {
      case None      => EmptyPath
      case Some('/') => AbsolutePath(parts.tail)
      case _         => RootlessPath(parts)
    }
  }

  implicit val eqUrlPath: Eq[UrlPath] = Eq.fromUniversalEquals
  implicit val showUrlPath: Show[UrlPath] = Show.fromToString
  implicit val orderUrlPath: Order[UrlPath] = Order.by(_.toString())
}

/**
  * This trait has two subclasses; `AbsolutePath` and `EmptyPath`.
  * This encompasses the paths allowed to be used in URLs that have an Authority. As per RFC 3986:
  *
  *   When authority is present, the path must either be empty or begin with a slash ("/") character.
  */
sealed trait AbsoluteOrEmptyPath extends UrlPath {
  def toAbsoluteOrEmpty: AbsoluteOrEmptyPath =
    this

  def toRootless: RootlessPath =
    RootlessPath(parts)
}
object AbsoluteOrEmptyPath {
  implicit val eqAbsoluteOrEmptyPath: Eq[AbsoluteOrEmptyPath] = Eq.fromUniversalEquals
  implicit val showAbsoluteOrEmptyPath: Show[AbsoluteOrEmptyPath] = Show.fromToString
  implicit val orderAbsoluteOrEmptyPath: Order[AbsoluteOrEmptyPath] = Order.by(_.toString())
}

case object EmptyPath extends AbsoluteOrEmptyPath {
  def isEmpty: Boolean =
    true

  def toAbsolute: AbsolutePath =
    AbsolutePath(Vector.empty)

  def withParts(parts: Iterable[String]): UrlPath =
    UrlPath(parts.toVector)

  def config: UriConfig =
    UriConfig.default

  def parts: Vector[String] =
    Vector.empty

  def unapply(path: UrlPath): Boolean =
    path.isEmpty

  override private[uri] def toString(c: UriConfig): String = ""
}

final case class RootlessPath(parts: Vector[String])(implicit val config: UriConfig = UriConfig.default)
    extends UrlPath {
  def toRootless: RootlessPath =
    this

  def toAbsolute: AbsolutePath =
    AbsolutePath(parts)

  def toAbsoluteOrEmpty: AbsoluteOrEmptyPath =
    if (parts.isEmpty) EmptyPath
    else AbsolutePath(parts)

  def withParts(otherParts: Iterable[String]): UrlPath =
    RootlessPath(otherParts.toVector)

  /**
    * Returns true if this path is empty (i.e. calling `toString` will return an empty String)
    */
  def isEmpty: Boolean =
    parts.isEmpty
}

object RootlessPath {
  def fromParts(parts: String*)(implicit config: UriConfig = UriConfig.default): RootlessPath =
    new RootlessPath(parts.toVector)

  implicit val eqRootlessPath: Eq[RootlessPath] = Eq.fromUniversalEquals
  implicit val showRootlessPath: Show[RootlessPath] = Show.fromToString
  implicit val orderRootlessPath: Order[RootlessPath] = Order.by(_.parts)
}

/**
  * An AbsolutePath is a path that starts with a slash
  */
final case class AbsolutePath(parts: Vector[String])(implicit val config: UriConfig = UriConfig.default)
    extends AbsoluteOrEmptyPath {
  def toAbsolute: AbsolutePath =
    this

  def withParts(otherParts: Iterable[String]): UrlPath =
    copy(parts = otherParts.toVector)

  /**
    * Always returns false as we always have at least a leading slash
    */
  def isEmpty: Boolean =
    false

  override private[uri] def toString(c: UriConfig): String =
    "/" + super.toString(c)
}

object AbsolutePath {
  def fromParts(parts: String*)(implicit config: UriConfig = UriConfig.default): AbsolutePath =
    new AbsolutePath(parts.toVector)

  implicit val eqAbsolutePath: Eq[AbsolutePath] = Eq.fromUniversalEquals
  implicit val showAbsolutePath: Show[AbsolutePath] = Show.fromToString
  implicit val orderAbsolutePath: Order[AbsolutePath] = Order.by(_.parts)
}

final case class UrnPath(nid: String, nss: String)(implicit val config: UriConfig = UriConfig.default) extends Path {
  def parts: Vector[String] =
    Vector(nid, nss)

  def toUrlPath: UrlPath =
    UrlPath(parts)

  def isEmpty: Boolean =
    false

  private[uri] def toString(c: UriConfig): String =
    nid + ":" + c.pathEncoder.encode(nss, c.charset)
}

object UrnPath {
  def parseTry(s: CharSequence)(implicit config: UriConfig = UriConfig.default): Try[UrnPath] =
    UrnParser.parseUrnPath(s.toString)

  def parseOption(s: CharSequence)(implicit config: UriConfig = UriConfig.default): Option[UrnPath] =
    parseTry(s).toOption

  def parse(s: CharSequence)(implicit config: UriConfig = UriConfig.default): UrnPath =
    parseTry(s).get

  implicit val eqUrnPath: Eq[UrnPath] = Eq.fromUniversalEquals
  implicit val showUrnPath: Show[UrnPath] = Show.fromToString
  implicit val orderUrnPath: Order[UrnPath] = Order.by { path =>
    (path.nid, path.nss)
  }
}
