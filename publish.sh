#!/usr/bin/env sh

sbt updatePublicSuffixes
git add jvm/src/main/resources/public_suffix_trie.json
git commit -m"Update public suffixes"

sbt +test clean coverageOff "project scalaUriJVM" +publishSigned "project scalaUriJS" +publishSigned
