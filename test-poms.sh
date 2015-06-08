#!/usr/bin/env bash

set -eux

for i in pom-*.xml
do
    mvn clean test -f $i
done
