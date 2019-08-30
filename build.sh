#!/usr/bin/env bash

rm -R target

sbt ';build'

docker build -t alekslitvinenk/doppler:latest . --no-cache
#docker push alekslitvinenk/doppler:latest