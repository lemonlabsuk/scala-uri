package io.lemonlabs.uri

import io.lemonlabs.uri.config.UriConfig
import io.lemonlabs.uri.parsing.UrlParser

import scala.util.Try

case class Authority(userInfo: UserInfo, host: Host, port: Option[Int])(implicit config: UriConfig) {

  def user: Option[String] = userInfo.user
  def password: Option[String] = userInfo.password

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
    val userInfo = for {
      userStr <- user
      userStrEncoded = c.userInfoEncoder.encode(userStr, c.charset)
      passwordStrEncoded = password.map(p => ":" + c.userInfoEncoder.encode(p, c.charset)).getOrElse("")
    } yield userStrEncoded + passwordStrEncoded + "@"

    userInfo.getOrElse("") + hostToString(host) + port.map(":" + _).getOrElse("")
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
    new Authority(UserInfo.empty, Host.parse(host), port = None)

  def apply(host: Host)(implicit config: UriConfig): Authority =
    new Authority(UserInfo.empty, host, port = None)

  def apply(host: String, port: Int)(implicit config: UriConfig): Authority =
    new Authority(UserInfo.empty, Host.parse(host), Some(port))

  def apply(host: Host, port: Int)(implicit config: UriConfig): Authority =
    new Authority(UserInfo.empty, host, Some(port))

  def parseTry(s: CharSequence)(implicit config: UriConfig = UriConfig.default): Try[Authority] =
    UrlParser.parseAuthority(s.toString)

  def parseOption(s: CharSequence)(implicit config: UriConfig = UriConfig.default): Option[Authority] =
    parseTry(s).toOption

  def parse(s: CharSequence)(implicit config: UriConfig = UriConfig.default): Authority =
    parseTry(s).get
}

case class UserInfo(user: Option[String], password: Option[String])
object UserInfo {
  def apply(user: String): UserInfo =
    new UserInfo(Some(user), password = None)

  def apply(user: String, password: String): UserInfo =
    new UserInfo(Some(user), Some(password))

  def empty = UserInfo(None, None)

  def parseTry(s: CharSequence)(implicit config: UriConfig = UriConfig.default): Try[UserInfo] =
    UrlParser.parseUserInfo(s.toString)

  def parseOption(s: CharSequence)(implicit config: UriConfig = UriConfig.default): Option[UserInfo] =
    parseTry(s).toOption

  def parse(s: CharSequence)(implicit config: UriConfig = UriConfig.default): UserInfo =
    parseTry(s).get
}
