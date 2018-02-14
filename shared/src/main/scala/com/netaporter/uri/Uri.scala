package com.netaporter.uri

import com.netaporter.uri.config.UriConfig
import com.netaporter.uri.parsing.UrlParser

import scala.collection.Seq

/**
  * Represents a URI. See [[https://www.ietf.org/rfc/rfc3986 RFC 3986]]
  *
  * Can either be a URL or a URN
  *
  * URLs will be one of these forms:
  *
  *  -           Absolute: `http://example.com`
  *  -  Protocol Relative: `//example.com`
  *  -  Without Authority: `mailto:example@example.com`
  *  -      Root Relative: `/index.html?a=b`
  *  -  Rootless Relative: `index.html?a=b`
  *  -  Rootless Relative
  *    (with doc segment): `../index.html?a=b`
  *
  * URNs will be in the form `urn:example:example2`
  *
 */
trait Uri {
  type Self <: Uri
  type SelfWithScheme <: Uri

  private[uri] def self: Self

  implicit def config: UriConfig

  def schemeOption: Option[String]
  def path: Path

  /**
    * Copies this Uri but with the scheme set as the given value.
    *
    * @param scheme the new scheme to set
    * @return a new Uri with the specified scheme
    */
  def withScheme(scheme: String): SelfWithScheme

  private[uri] def toString(config: UriConfig): String

  def toUrl: Url
  def toUrn: Urn

  /**
    * Converts to a `java.net.URI`
    *
    * This involves a `toString` and `URI.parse` because the specific `java.net.URI`
    * constructors do not deal properly with encoded elements
    *
    * @return a `java.net.URI` matching this `com.netaporter.uri.Uri`
    */
  def toJavaURI: java.net.URI =
    new java.net.URI(toString(config))

  /**
    * Returns the path with no encoders taking place (e.g. non ASCII characters will not be percent encoded)
    * @return String containing the raw path for this Uri
    */
  def toStringRaw: String =
    toString(config.withNoEncoding)

  override def toString: String =
    toString(config)
}

object Uri {

  def apply(javaUri: java.net.URI): Uri =
    parse(javaUri.toASCIIString)

  def parse(s: CharSequence)(implicit config: UriConfig = UriConfig.default): Uri =
    UrlParser.parseUri(s.toString)
}
