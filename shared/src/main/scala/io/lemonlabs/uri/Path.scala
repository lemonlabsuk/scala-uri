package io.lemonlabs.uri
import cats.{Eq, Order, Show}
import io.lemonlabs.uri.Path.SlashTermination
import io.lemonlabs.uri.Path.SlashTermination._
import io.lemonlabs.uri.UrlPath.slash
import io.lemonlabs.uri.config.UriConfig
import io.lemonlabs.uri.parsing.{UrlParser, UrnParser}
import io.lemonlabs.uri.typesafe.{PathPart, TraversablePathParts}
import io.lemonlabs.uri.typesafe.PathPart.ops._
import io.lemonlabs.uri.typesafe.TraversablePathParts.ops._

import scala.annotation.tailrec
import scala.util.Try

sealed trait Path extends Product with Serializable {
  def config: UriConfig
  def parts: Vector[String]
  private[uri] def toStringWithConfig(config: UriConfig): String

  def isEmpty: Boolean
  def nonEmpty: Boolean = !isEmpty

  /** Returns the path with no encoders taking place (e.g. non ASCII characters will not be percent encoded)
    * @return String containing the raw path for this Uri
    */
  def toStringRaw: String =
    toStringWithConfig(config.withNoEncoding)

  override def toString: String =
    toStringWithConfig(config)
}

object Path {
  sealed trait SlashTermination
  object SlashTermination {

    /** Leave all paths as they are */
    case object Off extends SlashTermination

    /** Remove all trailing slashes */
    case object RemoveForAll extends SlashTermination

    /** Ensure that an empty path is slash terminated and otherwise leave it unchanged.
      * This corresponds to the RFC3986 URL normalization specification
      */
    case object AddForEmptyPath extends SlashTermination

    /** Add a trailing slash if path is empty and remove all other trailing slashes */
    case object AddForEmptyPathRemoveOthers extends SlashTermination

    /** Ensure that all paths are slash terminated */
    case object AddForAll extends SlashTermination
  }

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
  type Self <: UrlPath
  def withParts(parts: Iterable[String]): UrlPath

  def toRootless: RootlessPath
  def toAbsolute: AbsolutePath
  def toAbsoluteOrEmpty: AbsoluteOrEmptyPath

  def withConfig(config: UriConfig): Self

  def addPart[P: PathPart](part: P): UrlPath =
    withParts(parts :+ part.path)

  def addParts[P: TraversablePathParts](otherParts: P): UrlPath =
    withParts(parts = parts ++ otherParts.toSeq)

  def addParts[P: PathPart](first: P, second: P, otherParts: P*): UrlPath =
    addParts((Vector(first, second) ++ otherParts).map(_.path))

  /** Returns the encoded path. By default non ASCII characters in the path are percent encoded.
    * @return String containing the path for this Uri
    */
  private[uri] def toStringWithConfig(c: UriConfig): String = {
    val encodedParts = parts.map(p => c.pathEncoder.encode(p, c.charset))
    encodedParts.mkString("/")
  }

  /** @return this path with dot segments removed according to section 5.2.4 Remove Dot Segments of
    *         <a href="http://www.ietf.org/rfc/rfc3986.txt">RFC 3986</a>
    */
  private[uri] def removeDotSegments: UrlPath = withParts(removeDotSegments(parts, Seq.empty))

  @tailrec
  private def removeDotSegments(parts: Seq[String], acc: Seq[String]): Iterable[String] = {
    parts match {
      case Nil                          => acc.reverse
      case "." +: rest if rest.isEmpty  => ("" +: acc).reverse
      case ".." +: rest if rest.isEmpty => ("" +: acc.drop(1)).reverse
      case "." +: rest                  => removeDotSegments(rest, acc)
      case ".." +: rest                 => removeDotSegments(rest, acc.drop(1))
      case part +: rest                 => removeDotSegments(rest, part +: acc)
    }
  }

  /** Returns this path normalized according to
    * <a href="http://www.ietf.org/rfc/rfc3986.txt">RFC 3986</a>
    */
  def normalize(removeEmptyParts: Boolean = false, slashTermination: SlashTermination = AddForEmptyPath): UrlPath = {
    val normalized0 = removeDotSegments
    val normalized1 = if (removeEmptyParts) normalized0.removeEmptyParts else normalized0
    val normalized2 = normalized1.slashTerminated(slashTermination)
    if (normalized2.parts.isEmpty && !normalized2.isSlashTerminated) {
      EmptyPath
    } else {
      normalized2
    }
  }

  def slashTerminated(slashTermination: SlashTermination): UrlPath = slashTermination match {
    case Off =>
      this
    case RemoveForAll =>
      if (isSlashTerminated) {
        if (parts.length > 1) withParts(parts.dropRight(1)) else EmptyPath
      } else this
    case AddForEmptyPathRemoveOthers =>
      if (isEmpty || parts == Vector("")) {
        slash
      } else if (isSlashTerminated) {
        withParts(parts.dropRight(1))
      } else this
    case AddForEmptyPath =>
      if (isEmpty || parts == Vector("")) slash else this
    case AddForAll =>
      if (isEmpty || parts == Vector("")) {
        slash
      } else if (!isSlashTerminated) {
        addPart("")
      } else this
  }

  def isSlashTerminated: Boolean

  def removeEmptyParts: UrlPath =
    withParts(parts = parts.filter(_.nonEmpty))
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

  /** Unlike `UrlPath.parse`, this method treats the supplied String as a raw path and does not
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

/** This trait has two subclasses; `AbsolutePath` and `EmptyPath`.
  * This encompasses the paths allowed to be used in URLs that have an Authority. As per RFC 3986:
  *
  *   When authority is present, the path must either be empty or begin with a slash ("/") character.
  */
sealed trait AbsoluteOrEmptyPath extends UrlPath {
  type Self <: AbsoluteOrEmptyPath
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
  type Self = EmptyPath.type
  def isEmpty: Boolean =
    true

  def withConfig(config: UriConfig): EmptyPath.type =
    this

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

  override private[uri] def toStringWithConfig(c: UriConfig): String = ""

  override def isSlashTerminated: Boolean = false
}

final case class RootlessPath(parts: Vector[String])(implicit val config: UriConfig = UriConfig.default)
    extends UrlPath {
  type Self = RootlessPath
  def toRootless: RootlessPath =
    this

  def toAbsolute: AbsolutePath =
    AbsolutePath(parts)

  def toAbsoluteOrEmpty: AbsoluteOrEmptyPath =
    if (parts.isEmpty) EmptyPath
    else AbsolutePath(parts)

  def withConfig(config: UriConfig): RootlessPath =
    RootlessPath(parts)(config)

  def withParts(otherParts: Iterable[String]): UrlPath =
    RootlessPath(otherParts.toVector)

  /** Returns true if this path is empty (i.e. calling `toString` will return an empty String)
    */
  def isEmpty: Boolean =
    parts.isEmpty

  override def isSlashTerminated: Boolean = parts.lastOption.contains("")
}

object RootlessPath {
  def fromParts(parts: String*)(implicit config: UriConfig = UriConfig.default): RootlessPath =
    new RootlessPath(parts.toVector)

  implicit val eqRootlessPath: Eq[RootlessPath] = Eq.fromUniversalEquals
  implicit val showRootlessPath: Show[RootlessPath] = Show.fromToString
  implicit val orderRootlessPath: Order[RootlessPath] = Order.by(_.parts)
}

/** An AbsolutePath is a path that starts with a slash
  */
final case class AbsolutePath(parts: Vector[String])(implicit val config: UriConfig = UriConfig.default)
    extends AbsoluteOrEmptyPath {
  type Self = AbsolutePath
  def toAbsolute: AbsolutePath =
    this

  def withParts(otherParts: Iterable[String]): UrlPath =
    copy(parts = otherParts.toVector)

  def withConfig(config: UriConfig): AbsolutePath =
    AbsolutePath(parts)(config)

  /** Always returns false as we always have at least a leading slash
    */
  def isEmpty: Boolean =
    false

  override private[uri] def toStringWithConfig(c: UriConfig): String =
    "/" + super.toStringWithConfig(c)

  override def isSlashTerminated: Boolean =
    parts.lastOption.fold(true)(_ == "")

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

  def withConfig(config: UriConfig): UrnPath =
    UrnPath(nid, nss)(config)

  private[uri] def toStringWithConfig(c: UriConfig): String =
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
