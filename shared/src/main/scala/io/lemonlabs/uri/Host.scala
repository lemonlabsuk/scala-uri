package io.lemonlabs.uri

import io.lemonlabs.uri.config.UriConfig
import io.lemonlabs.uri.inet._
import io.lemonlabs.uri.parsing.UrlParser

import scala.annotation.tailrec
import scala.collection.immutable
import scala.util.Try

sealed trait Host extends PublicSuffixSupport {
  def value: String
  override def toString: String = value

  /**
    * @return the domain name in ASCII Compatible Encoding (ACE), as defined by the ToASCII
    *         operation of <a href="http://www.ietf.org/rfc/rfc3490.txt">RFC 3490</a>.
    */
  def toStringPunycode: String = value

  /**
    * Returns the apex domain for this Host.
    *
    * The apex domain is constructed from the public suffix prepended with the immediately preceding
    * dot segment.
    *
    * Examples include:
    *  `example.com`   for `www.example.com`
    *  `example.co.uk` for `www.example.co.uk`
    *
    * @return the apex domain for this domain
    */
  def apexDomain: Option[String]

  /**
    * Returns the second largest subdomain for this URL's host.
    *
    * E.g. for http://a.b.c.example.com returns a.b.c
    *
    * Note: In the event there is only one subdomain (i.e. the host is the apex domain), this method returns `None`.
    * E.g. This method will return `None` for `http://example.com`.
    *
    * @return the second largest subdomain for this URL's host
    */
  def subdomain: Option[String]

  /**
    * Returns all subdomains for this URL's host.
    * E.g. for http://a.b.c.example.com returns a, a.b, a.b.c and a.b.c.example
    *
    * @return all subdomains for this URL's host
    */
  def subdomains: Vector[String]

  /**
    * Returns the shortest subdomain for this URL's host.
    * E.g. for http://a.b.c.example.com returns a
    *
    * @return the shortest subdomain for this URL's host
    */
  def shortestSubdomain: Option[String]

  /**
    * Returns the longest subdomain for this URL's host.
    * E.g. for http://a.b.c.example.com returns a.b.c.example
    *
    * @return the longest subdomain for this URL's host
    */
  def longestSubdomain: Option[String]
}

object Host {
  def parseTry(s: CharSequence)(implicit config: UriConfig = UriConfig.default): Try[Host] =
    UrlParser.parseHost(s.toString)

  def parseOption(s: CharSequence)(implicit config: UriConfig = UriConfig.default): Option[Host] =
    parseTry(s).toOption

  def parse(s: CharSequence)(implicit config: UriConfig = UriConfig.default): Host =
    parseTry(s).get

  def unapply(host: Host): Option[String] =
    Some(host.toString)
}

final case class DomainName(value: String) extends Host with PublicSuffixSupportImpl with PunycodeSupport {
  /**
    * @return the domain name in ASCII Compatible Encoding (ACE), as defined by the ToASCII
    *         operation of <a href="http://www.ietf.org/rfc/rfc3490.txt">RFC 3490</a>.
    */
  override def toStringPunycode: String =
    toPunycode(value)

  /**
    * Returns the apex domain for this Host.
    *
    * The apex domain is constructed from the public suffix prepended with the immediately preceding
    * dot segment.
    *
    * Examples include:
    *  `example.com`   for `www.example.com`
    *  `example.co.uk` for `www.example.co.uk`
    *
    * @return the apex domain for this domain
    */
  def apexDomain: Option[String] =
    publicSuffix map { ps =>
      val apexDomainStart = value.dropRight(ps.length + 1).lastIndexOf('.')

      if (apexDomainStart == -1) value
      else value.substring(apexDomainStart + 1)
    }

  /**
    * Returns the second largest subdomain in this host.
    *
    * E.g. for http://a.b.c.example.com returns a.b.c
    *
    * Note: In the event there is only one subdomain (i.e. the host is the apex domain), this method returns `None`.
    * E.g. This method will return `None` for `http://example.com`.
    *
    * @return the second largest subdomain for this host
    */
  def subdomain: Option[String] = longestSubdomain flatMap { ls =>
    ls.lastIndexOf('.') match {
      case -1 => None
      case i  => Some(ls.substring(0, i))
    }
  }

  /**
    * Returns all subdomains for this host.
    * E.g. for http://a.b.c.example.com returns a, a.b, a.b.c and a.b.c.example
    *
    * @return all subdomains for this host
    */
  def subdomains: Vector[String] = {
    def concatHostParts(longestSubdomainStr: String) = {
      val parts = longestSubdomainStr.split('.').toVector
      if (parts.size == 1) parts
      else {
        parts.tail.foldLeft(Vector(parts.head)) { (subdomainList, part) =>
          subdomainList :+ (subdomainList.last + '.' + part)
        }
      }
    }

    longestSubdomain.map(concatHostParts).getOrElse(Vector.empty)
  }

  /**
    * Returns the shortest subdomain for this host.
    * E.g. for http://a.b.c.example.com returns a
    *
    * @return the shortest subdomain for this host
    */
  def shortestSubdomain: Option[String] =
    longestSubdomain.map(_.takeWhile(_ != '.'))

  /**
    * Returns the longest subdomain for this host.
    * E.g. for http://a.b.c.example.com returns a.b.c.example
    *
    * @return the longest subdomain for this host
    */
  def longestSubdomain: Option[String] = {
    val publicSuffixLength: Int = publicSuffix.map(_.length + 1).getOrElse(0)
    value.dropRight(publicSuffixLength) match {
      case ""    => None
      case other => Some(other)
    }
  }
}

object DomainName {
  def parseTry(s: CharSequence)(implicit config: UriConfig = UriConfig.default): Try[DomainName] =
    UrlParser.parseDomainName(s.toString)

  def parseOption(s: CharSequence)(implicit config: UriConfig = UriConfig.default): Option[DomainName] =
    parseTry(s).toOption

  def parse(s: CharSequence)(implicit config: UriConfig = UriConfig.default): DomainName =
    parseTry(s).get

  def empty: DomainName = DomainName("")
}

final case class IpV4(octet1: Byte, octet2: Byte, octet3: Byte, octet4: Byte) extends Host {
  private def uByteToInt(b: Byte): Int = b & 0xff

  def octet1Int: Int = uByteToInt(octet1)
  def octet2Int: Int = uByteToInt(octet2)
  def octet3Int: Int = uByteToInt(octet3)
  def octet4Int: Int = uByteToInt(octet4)

  def octets: Vector[Byte] = Vector(octet1, octet2, octet3, octet4)
  def octetsInt: Vector[Int] = Vector(octet1Int, octet2Int, octet3Int, octet4Int)

  def value: String = s"$octet1Int.$octet2Int.$octet3Int.$octet4Int"

  def apexDomain: Option[String] = None
  def publicSuffix: Option[String] = None
  def publicSuffixes: Vector[String] = Vector.empty
  def subdomain: Option[String] = None
  def subdomains: Vector[String] = Vector.empty
  def shortestSubdomain: Option[String] = None
  def longestSubdomain: Option[String] = None
}

object IpV4 {
  def apply(octet1: Int, octet2: Int, octet3: Int, octet4: Int): IpV4 = {
    require(octet1 >= 0 && octet2 >= 0 && octet3 >= 0 && octet4 >= 0, "Octets must be >= 0")
    require(octet1 <= 255 && octet2 <= 255 && octet3 <= 255 && octet4 <= 255, "Octets must be <= 255")

    new IpV4(octet1.toByte, octet2.toByte, octet3.toByte, octet4.toByte)
  }

  def parseTry(s: CharSequence)(implicit config: UriConfig = UriConfig.default): Try[IpV4] =
    UrlParser.parseIpV4(s.toString)

  def parseOption(s: CharSequence)(implicit config: UriConfig = UriConfig.default): Option[IpV4] =
    parseTry(s).toOption

  def parse(s: CharSequence)(implicit config: UriConfig = UriConfig.default): IpV4 =
    parseTry(s).get
}

final case class IpV6(piece1: Char,
                      piece2: Char,
                      piece3: Char,
                      piece4: Char,
                      piece5: Char,
                      piece6: Char,
                      piece7: Char,
                      piece8: Char)
    extends Host {
  def piece1Int: Int = piece1.toInt
  def piece2Int: Int = piece2.toInt
  def piece3Int: Int = piece3.toInt
  def piece4Int: Int = piece4.toInt
  def piece5Int: Int = piece5.toInt
  def piece6Int: Int = piece6.toInt
  def piece7Int: Int = piece7.toInt
  def piece8Int: Int = piece8.toInt

  val pieces: Vector[Char] = Vector(piece1, piece2, piece3, piece4, piece5, piece6, piece7, piece8)
  def hexPieces: Vector[String] = pieces.map(hex)

  private def hex(c: Char): String = Integer.toHexString(c.toInt)

  /**
    * Finds the longest runs of two or more zeros in this IPv6
    * Returns the start and end index of the run
    * Returns (-1, -1) if there is no run
    */
  private def elidedStartAndEnd(): (Int, Int) = {
    @tailrec def longestRun(index: Int, longest: (Int, Int), currentRunStart: Int): (Int, Int) = {
      def newLongest = {
        val newLength = index - currentRunStart
        if (newLength > 1 && newLength > (longest._2 - longest._1)) (currentRunStart, index) else longest
      }

      if (index == 8)
        newLongest
      else if (pieces(index) != 0)
        longestRun(index + 1, newLongest, index + 1)
      else
        longestRun(index + 1, longest, currentRunStart)
    }

    longestRun(0, (-1, -1), 0)
  }

  def apexDomain: Option[String] = None
  def publicSuffix: Option[String] = None
  def publicSuffixes: Vector[String] = Vector.empty
  def subdomain: Option[String] = None
  def subdomains: Vector[String] = Vector.empty
  def shortestSubdomain: Option[String] = None
  def longestSubdomain: Option[String] = None

  def value: String = elidedStartAndEnd() match {
    case (-1, -1) => toStringNonNormalised
    case (start, end) =>
      "[" + hexPieces.take(start).mkString(":") + "::" + hexPieces.drop(end).mkString(":") + "]"
  }

  def toStringNonNormalised: String = hexPieces.mkString("[", ":", "]")
}

object IpV6 {
  def apply(piece1: Int,
            piece2: Int,
            piece3: Int,
            piece4: Int,
            piece5: Int,
            piece6: Int,
            piece7: Int,
            piece8: Int): IpV6 = {
    require(
      piece1 >= 0 && piece2 >= 0 && piece3 >= 0 && piece4 >= 0 &&
        piece5 >= 0 && piece6 >= 0 && piece7 >= 0 && piece8 >= 0,
      "IPv6 pieces must be >= 0"
    )
    require(
      piece1 <= Char.MaxValue && piece2 <= Char.MaxValue && piece3 <= Char.MaxValue && piece4 <= Char.MaxValue &&
        piece5 <= Char.MaxValue && piece6 <= Char.MaxValue && piece7 <= Char.MaxValue && piece8 <= Char.MaxValue,
      "IPv6 Pieces must be <= " + Char.MaxValue.toInt
    )
    new IpV6(
      piece1.toChar,
      piece2.toChar,
      piece3.toChar,
      piece4.toChar,
      piece5.toChar,
      piece6.toChar,
      piece7.toChar,
      piece8.toChar
    )
  }

  def apply(piece1: String,
            piece2: String,
            piece3: String,
            piece4: String,
            piece5: String,
            piece6: String,
            piece7: String,
            piece8: String): IpV6 = {
    def hexToInt(hex: String) = Integer.parseInt(hex, 16)
    IpV6(
      hexToInt(piece1),
      hexToInt(piece2),
      hexToInt(piece3),
      hexToInt(piece4),
      hexToInt(piece5),
      hexToInt(piece6),
      hexToInt(piece7),
      hexToInt(piece8)
    )
  }

  def fromIntPieces(pieces: immutable.Seq[Int]): IpV6 = {
    require(pieces.length == 8, "IPv6 must be made up of eight pieces")
    IpV6(pieces(0), pieces(1), pieces(2), pieces(3), pieces(4), pieces(5), pieces(6), pieces(7))
  }

  def fromHexPieces(pieces: immutable.Seq[String]): IpV6 = {
    require(pieces.length == 8, "IPv6 must be made up of eight pieces")
    IpV6(pieces(0), pieces(1), pieces(2), pieces(3), pieces(4), pieces(5), pieces(6), pieces(7))
  }

  def parseTry(s: CharSequence)(implicit config: UriConfig = UriConfig.default): Try[IpV6] =
    UrlParser.parseIpV6(s.toString)

  def parseOption(s: CharSequence)(implicit config: UriConfig = UriConfig.default): Option[IpV6] =
    parseTry(s).toOption

  def parse(s: CharSequence)(implicit config: UriConfig = UriConfig.default): IpV6 =
    parseTry(s).get
}
