#!/usr/bin/env bash

rm -R target

sbt ';build'

docker build -t alekslitvinenk/doppler:edge6 . --no-cache
docker push alekslitvinenk/doppler:edge6