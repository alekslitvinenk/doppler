#!/usr/bin/env bash

rm -R target

./bump.sh "$@"

version=$(cat VERSION)
ver="v$version"

sbt ';build' && \
docker build -t alekslitvinenk/doppler:"$ver" -t alekslitvinenk/doppler:latest . --no-cache && \
docker push alekslitvinenk/doppler:"$ver"

docker build -t alekslitvinenk/git-clone:"$ver" -t alekslitvinenk/git-clone:latest . -f GitClone.Dockerfile --no-cache && \
docker push alekslitvinenk/git-clone:"$ver"