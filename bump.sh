#!/usr/bin/env bash

#
# Copyright (c) 2023. Dockovpn Solutions. All Rights Reserved
#

version=$(cat VERSION)

IFS='.' read -r -a components <<< "$version"

major_=${components[0]}
minor_=${components[1]}
patch_=${components[2]}

echo "old version: $major_.$minor_.$patch_"

function bumpVersion() {
  local op=$1
  local new_major_=$major_
  local new_minor_=$minor_
  local new_patch_=$patch_

  case $op in
    major)
      new_major_=$(($major_ + 1))
      new_minor_=0
      new_patch_=0
      ;;

    minor)
      new_minor_=$(($minor_ + 1))
      new_patch_=0
      ;;

    patch)
      new_patch_=$(($patch_ + 1))
      ;;

    *)
      echo "Unrecognized version component"
      exit 1
      ;;
  esac

  new_version="$new_major_.$new_minor_.$new_patch_"
}

bumpVersion "$@" || exit 1


echo "$new_version"
echo "$new_version" > "VERSION"