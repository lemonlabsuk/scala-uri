#!/usr/bin/env sh

sbt updatePublicSuffixes
git add shared/src/main/scala/io/lemonlabs/uri/inet/PublicSuffixTrie.scala
git commit -m"Update public suffixes"

sbt +test clean coverageOff +publishSigned

