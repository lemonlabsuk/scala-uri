package io.lemonlabs.uri.inet

@deprecated("scala-uri no longer uses a Trie for public suffixes. This object will be removed", "2.2.2")
object PublicSuffixTrie {
  @deprecated("scala-uri no longer uses a Trie for public suffixes. This variable will be removed", "2.2.2")
  lazy val publicSuffixTrie = Trie.Empty
}
