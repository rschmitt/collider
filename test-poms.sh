#!/usr/bin/env bash

set -eux

versions=(1.5.1 1.6.0 1.7.0-RC1)

for i in ${versions[@]}
do
    cp pom.xml pom-$i.xml
    perl -i -pe 's/\[1.5.0,\)/'"$i"'/g' pom-$i.xml
    mvn clean test -f pom-$i.xml
done

for i in ${versions[@]}
do
    rm pom-$i.xml
done
