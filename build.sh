#!/usr/bin/env bash

rm -R target

./bump.sh "$@"

version=$(cat VERSION)
ver="v$version"

sbt ';build' && \
docker build -t alekslitvinenk/doppler:"$ver" -t alekslitvinenk/doppler:latest . --no-cache && \
docker push alekslitvinenk/doppler:"$ver"