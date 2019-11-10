package io.lemonlabs.uri

import cats.implicits._
import cats.{Eq, Order, Show}
import io.lemonlabs.uri.config.UriConfig
import io.lemonlabs.uri.parsing.UrlParser

import scala.util.Try

case class Authority(userInfo: Option[UserInfo], host: Host, port: Option[Int])(
    implicit config: UriConfig = UriConfig.default
) {

  def user: Option[String] = userInfo.map(_.user)
  def password: Option[String] = userInfo.flatMap(_.password)

  /**
    * Returns the longest public suffix for the host in this URI. Examples include:
    *  `com`   for `www.example.com`
    *  `co.uk` for `www.example.co.uk`
    *
    * @return the longest public suffix for the host in this URI
    */
  def publicSuffix: Option[String] =
    host.publicSuffix

  /**
    * Returns all longest public suffixes for the host in this URI. Examples include:
    *  `com` for `www.example.com`
    *  `co.uk` and `uk` for `www.example.co.uk`
    *
    * @return all public suffixes for the host in this URI
    */
  def publicSuffixes: Vector[String] =
    host.publicSuffixes

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
  def subdomain: Option[String] =
    host.subdomain

  /**
    * Returns all subdomains for this URL's host.
    * E.g. for http://a.b.c.example.com returns a, a.b, a.b.c and a.b.c.example
    *
    * @return all subdomains for this URL's host
    */
  def subdomains: Vector[String] =
    host.subdomains

  /**
    * Returns the shortest subdomain for this URL's host.
    * E.g. for http://a.b.c.example.com returns a
    *
    * @return the shortest subdomain for this URL's host
    */
  def shortestSubdomain: Option[String] =
    host.shortestSubdomain

  /**
    * Returns the longest subdomain for this URL's host.
    * E.g. for http://a.b.c.example.com returns a.b.c.example
    *
    * @return the longest subdomain for this URL's host
    */
  def longestSubdomain: Option[String] =
    host.longestSubdomain

  private[uri] def toString(c: UriConfig, hostToString: Host => String): String = {
    val userInfoStr = userInfo.map(_.toString(c) + "@").getOrElse("")
    userInfoStr + hostToString(host) + port.map(":" + _).getOrElse("")
  }

  /**
    * @return the domain name in ASCII Compatible Encoding (ACE), as defined by the ToASCII
    *         operation of <a href="http://www.ietf.org/rfc/rfc3490.txt">RFC 3490</a>.
    */
  def toStringPunycode: String =
    toString(config, _.toStringPunycode)

  override def toString: String =
    toString(config, _.toString)

  def toStringRaw: String =
    toString(config.withNoEncoding, _.toString)
}

object Authority {

  def apply(host: String)(implicit config: UriConfig): Authority =
    new Authority(None, Host.parse(host), port = None)

  def apply(host: Host)(implicit config: UriConfig): Authority =
    new Authority(None, host, port = None)

  def apply(host: String, port: Int)(implicit config: UriConfig): Authority =
    new Authority(None, Host.parse(host), Some(port))

  def apply(host: Host, port: Int)(implicit config: UriConfig): Authority =
    new Authority(None, host, Some(port))

  def parseTry(s: CharSequence)(implicit config: UriConfig = UriConfig.default): Try[Authority] =
    UrlParser.parseAuthority(s.toString)

  def parseOption(s: CharSequence)(implicit config: UriConfig = UriConfig.default): Option[Authority] =
    parseTry(s).toOption

  def parse(s: CharSequence)(implicit config: UriConfig = UriConfig.default): Authority =
    parseTry(s).get

  implicit val eqAuthority: Eq[Authority] = Eq.fromUniversalEquals
  implicit val showAuthority: Show[Authority] = Show.fromToString
  implicit val orderAuthority: Order[Authority] = Order.by { authority =>
    (authority.userInfo, authority.host, authority.port)
  }
}

case class UserInfo(user: String, password: Option[String])(
    implicit config: UriConfig = UriConfig.default
) {
  private[uri] def toString(c: UriConfig): String = {
    val userStrEncoded = c.userInfoEncoder.encode(user, c.charset)
    val passwordStrEncoded = password.map(p => ":" + c.userInfoEncoder.encode(p, c.charset)).getOrElse("")
    userStrEncoded + passwordStrEncoded
  }

  override def toString: String =
    toString(config)

  def toStringRaw: String =
    toString(config.withNoEncoding)
}
object UserInfo {
  def apply(user: String): UserInfo =
    new UserInfo(user, password = None)

  def apply(user: String, password: String): UserInfo =
    new UserInfo(user, Some(password))

  def parseTry(s: CharSequence)(implicit config: UriConfig = UriConfig.default): Try[UserInfo] =
    UrlParser.parseUserInfo(s.toString)

  def parseOption(s: CharSequence)(implicit config: UriConfig = UriConfig.default): Option[UserInfo] =
    parseTry(s).toOption

  def parse(s: CharSequence)(implicit config: UriConfig = UriConfig.default): UserInfo =
    parseTry(s).get

  implicit val eqUserInfo: Eq[UserInfo] = Eq.fromUniversalEquals
  implicit val showUserInfo: Show[UserInfo] = Show.fromToString
  implicit val orderUserInfo: Order[UserInfo] = Order.by { userInfo =>
    (userInfo.user, userInfo.password)
  }
}
