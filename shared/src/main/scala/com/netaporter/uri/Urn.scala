package com.netaporter.uri
import com.netaporter.uri.config.UriConfig
import com.netaporter.uri.parsing.UrlParser

/**
  * Represents a URN. See [[https://www.ietf.org/rfc/rfc2141 RFC 2141]]
  * and [[https://tools.ietf.org/html/rfc8141 RFC 8141]]
  *
  * URNs will be in the form `urn:nid:nss`
  */
case class Urn(path: UrnPath)(implicit val config: UriConfig = UriConfig.default) extends Uri {

  type Self = Urn
  type SelfWithScheme = UrlWithoutAuthority

  def self: Self = this

  def schemeOption = Some("urn")

  /**
    * Converts this URN into a URL with the given scheme.
    * The NID and NSS will be made path segments of the URL.
    *
    * @param scheme the new scheme to set
    * @return a new Uri with the specified scheme
    */
  def withScheme(scheme: String): UrlWithoutAuthority =
    UrlWithoutAuthority(scheme, path.toUrlPath, QueryString.empty, fragment = None)

  def toUrl: Url = throw new IllegalStateException("Urn cannot be cast to Url")
  def toUrn: Urn = this

  private[uri] def toString(c: UriConfig): String =
    "urn:" + path.toString(c)
}

object Urn {
  def parse(s: CharSequence)(implicit config: UriConfig = UriConfig.default): Urn =
    UrlParser.parseUrn(s.toString)
}

case class UrnPath(nid: String, nss: String)(implicit val config: UriConfig = UriConfig.default) extends Path {

  def parts: Vector[String] =
    Vector(nid, nss)

  def toUrlPath: UrlPath =
    UrlPath(parts, leadingSlash = false)

  private[uri] def toString(c: UriConfig): String =
    c.pathEncoder.encode(nid, c.charset) + ":" + c.pathEncoder.encode(nss, c.charset)
}

object UrnPath {
  def parse(s: CharSequence)(implicit config: UriConfig = UriConfig.default): UrnPath =
    UrlParser.parseUrnPath(s.toString)
}