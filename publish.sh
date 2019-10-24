#!/usr/bin/env sh

sbt updatePublicSuffixes
git add jvm/src/main/resources/public_suffix_trie.json
git commit -m"Update public suffixes"

sbt +test clean coverageOff +publishSigned

