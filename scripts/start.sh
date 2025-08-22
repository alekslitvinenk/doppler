#!/usr/bin/env bash

git clone --depth 5 "$GIT_REPO" "$DESTINATION_DIR"
cd "$DESTINATION_DIR" || exit
git checkout "$GIT_COMMIT"
