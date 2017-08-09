#!/usr/bin/env sh

sbt ";project scalaUriJVM; +publishSigned; project scalaUriJS; +publishSigned"
