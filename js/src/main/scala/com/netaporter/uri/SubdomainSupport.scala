package com.netaporter.uri

import scala.collection.Seq

trait SubdomainSupport { this: Uri =>

  /**
    * Unable to do correct subdomain support for scala-js until `PublicSuffixes` is ported.
    * This method exists for backwards compatibility
    */
  def subdomain = hostParts.headOption

  // Methods below are unsupported for scala-js until `PublicSuffixes` is ported

  def subdomains: Seq[String] = Vector.empty
  def shortestSubdomain: Option[String] = None
  def longestSubdomain: Option[String] = None

}
