#!/usr/bin/env bash

mvn clean package -Dmaven.test.skip="true"\
  && cd target \
  && sh build_docker_image.sh "$1"