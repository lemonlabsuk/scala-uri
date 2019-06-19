package io.lemonlabs.uri.inet

import io.lemonlabs.uri.Host

trait PublicSuffixSupportImpl { self: Host =>

  /**
    * Returns the longest public suffix for the host in this URI. Examples include:
    *  `com`   for `www.example.com`
    *  `co.uk` for `www.example.co.uk`
    *
    * @return the longest public suffix for the host in this URI
    */
  def publicSuffix: Option[String] =
    conf.jsonSupport.publicSuffixTrie.longestMatch(value.reverse).map(_.reverse)

  /**
    * Returns all longest public suffixes for the host in this URI. Examples include:
    *  `com` for `www.example.com`
    *  `co.uk` and `uk` for `www.example.co.uk`
    *
    * @return all public suffixes for the host in this URI
    */
  def publicSuffixes: Vector[String] =
    conf.jsonSupport.publicSuffixTrie.matches(value.reverse).map(_.reverse)
}
