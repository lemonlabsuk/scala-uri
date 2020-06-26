#!/usr/bin/env sh

sbt +test +clean +coverageOff +publishSigned
