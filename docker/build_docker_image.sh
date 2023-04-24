#!/usr/bin/env bash

#get_arch=`arch`
#docker build -f Dockerfile-$get_arch -t @docker.image.name@:$get_arch-@project.version@ .

if [ "$1" == "test" ] || [ "$1" != "publish" ] && ( [[ "@project.version@" == *"snapshot" ]] || [[ "@project.version@" == *"SNAPSHOT" ]] )
then
  docker build -t coopersoft/@project.name@:@project.version@ .
else
  docker buildx build . --platform linux/amd64,linux/arm64 --push -t coopersoft/@project.name@:@project.version@
fi