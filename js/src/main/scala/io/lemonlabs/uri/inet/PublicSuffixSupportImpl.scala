package io.lemonlabs.uri.inet

import io.lemonlabs.uri.{Host, NotImplementedForScalaJsError}

object PublicSuffixSupportImpl {
  lazy val trie: Trie =
    throw NotImplementedForScalaJsError
}

trait PublicSuffixSupportImpl { self: Host =>
  /**
    * Returns the longest public suffix for the host in this URI. Examples include:
    *  `com`   for `www.example.com`
    *  `co.uk` for `www.example.co.uk`
    *
    * @return the longest public suffix for the host in this URI
    */
  def publicSuffix: Option[String] =
    throw NotImplementedForScalaJsError

  /**
    * Returns all longest public suffixes for the host in this URI. Examples include:
    *  `com` for `www.example.com`
    *  `co.uk` and `uk` for `www.example.co.uk`
    *
    * @return all public suffixes for the host in this URI
    */
  def publicSuffixes: Vector[String] =
    throw NotImplementedForScalaJsError
}
