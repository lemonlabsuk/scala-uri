package com.netaporter.uri

import scala.collection.Seq

trait SubdomainSupport { this: Uri =>
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
  def subdomain = longestSubdomain flatMap { ls =>
    ls.lastIndexOf('.') match {
      case -1 => None
      case i  => Some(ls.substring(0, i))
    }
  }

  /**
    * Returns all subdomains for this URL's host.
    * E.g. for http://a.b.c.example.com returns a, a.b, a.b.c and a.b.c.example
    * @return all subdomains for this URL's host
    */
  def subdomains: Seq[String] = {
    def concatHostParts(longestSubdomainStr: String) = {
      val parts = longestSubdomainStr.split('.').toVector
      if(parts.size == 1) parts
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
    * @return the shortest subdomain for this URL's host
    */
  def shortestSubdomain: Option[String] =
    longestSubdomain.map(_.takeWhile( _ != '.'))

  /**
    * Returns the longest subdomain for this URL's host.
    * E.g. for http://a.b.c.example.com returns a.b.c.example
    * @return the longest subdomain for this URL's host
    */
  def longestSubdomain: Option[String] = for {
    h <- host
    publicSuffixLength = publicSuffix.map(_.length + 1).getOrElse(0)
    longestSubdomainStr <- h.dropRight(publicSuffixLength) match {
      case "" => None
      case other => Some(other)
    }
  } yield longestSubdomainStr
}
