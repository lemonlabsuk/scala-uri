#!/usr/bin/env sh

sbt clean coverageOff "project scalaUriJVM" +publishSigned "project scalaUriJS" +publishSigned
