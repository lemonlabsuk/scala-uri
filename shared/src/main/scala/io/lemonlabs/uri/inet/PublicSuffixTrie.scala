package io.lemonlabs.uri.inet

@deprecated("scala-uri no longer uses a Trie for public suffixes. This object will be removed")
object PublicSuffixTrie {
  @deprecated("scala-uri no longer uses a Trie for public suffixes. This variable will be removed")
  lazy val publicSuffixTrie = Trie.Empty
}
