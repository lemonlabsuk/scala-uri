package io.lemonlabs.uri

import io.lemonlabs.uri.config.UriConfig
import io.lemonlabs.uri.parsing.UrlParser


case class Authority(userInfo: UserInfo,
                     host: Host,
                     port: Option[Int])
                    (implicit config: UriConfig) {

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
    * Note: In the event there is only one subdomain (i.e. the host is the root domain), this method returns `None`.
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

  private[uri] def toString(c: UriConfig): String = {
    val userInfo = for {
      userStr <- user
      userStrEncoded = c.userInfoEncoder.encode(userStr, c.charset)
      passwordStrEncoded = password.map(p => ":" + c.userInfoEncoder.encode(p, c.charset)).getOrElse("")
    } yield userStrEncoded + passwordStrEncoded + "@"

    userInfo.getOrElse("") + host + port.map(":" + _).getOrElse("")
  }

  override def toString: String =
    toString(config)

  def toStringRaw: String =
    toString(config.withNoEncoding)
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

  def parse(s: CharSequence)(implicit config: UriConfig = UriConfig.default): Authority =
    UrlParser.parseAuthority(s.toString)
}

case class UserInfo(user: Option[String], password: Option[String])
object UserInfo {
  def apply(user: String): UserInfo =
    new UserInfo(Some(user), password = None)

  def apply(user: String, password: String): UserInfo =
    new UserInfo(Some(user), Some(password))

  def empty = UserInfo(None, None)

  def parse(s: CharSequence)(implicit config: UriConfig = UriConfig.default): UserInfo =
    UrlParser.parseUserInfo(s.toString)
}
