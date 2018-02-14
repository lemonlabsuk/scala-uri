package com.netaporter.uri

import com.netaporter.uri.inet.PublicSuffixSupport
import com.netaporter.uri.config.UriConfig
import com.netaporter.uri.parsing.UrlParser


case class Authority(userInfo: UserInfo,
                     host: String,
                     port: Option[Int])
                    (implicit config: UriConfig) extends PublicSuffixSupport {

  def user: Option[String] = userInfo.user
  def password: Option[String] = userInfo.password

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
  def subdomain: Option[String] = longestSubdomain flatMap { ls =>
    ls.lastIndexOf('.') match {
      case -1 => None
      case i => Some(ls.substring(0, i))
    }
  }

  /**
    * Returns all subdomains for this URL's host.
    * E.g. for http://a.b.c.example.com returns a, a.b, a.b.c and a.b.c.example
    *
    * @return all subdomains for this URL's host
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
    * Returns the shortest subdomain for this URL's host.
    * E.g. for http://a.b.c.example.com returns a
    *
    * @return the shortest subdomain for this URL's host
    */
  def shortestSubdomain: Option[String] =
    longestSubdomain.map(_.takeWhile(_ != '.'))

  /**
    * Returns the longest subdomain for this URL's host.
    * E.g. for http://a.b.c.example.com returns a.b.c.example
    *
    * @return the longest subdomain for this URL's host
    */
  def longestSubdomain: Option[String] = {
    val publicSuffixLength: Int = publicSuffix.map(_.length + 1).getOrElse(0)
    host.dropRight(publicSuffixLength) match {
      case "" => None
      case other => Some(other)
    }
  }

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
    new Authority(UserInfo.empty, host, port = None)

  def apply(host: String, port: Int)(implicit config: UriConfig): Authority =
    new Authority(UserInfo.empty, host, Some(port))

  def empty(implicit uriConfig: UriConfig): Authority =
    Authority(UserInfo.empty, "", None)

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
