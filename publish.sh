#!/usr/bin/env sh

sbt +test clean coverageOff +publishSigned && \
SCALAJS_VERSION=0.6.32 sbt +test clean coverageOff +scalaUriJS/publishSigned
