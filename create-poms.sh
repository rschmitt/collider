#!/usr/bin/env bash

set -eux

for i in 1.5.1 1.6.0 1.7.0-RC1
do
    cp pom.xml pom-$i.xml
    perl -i -pe 's/\[1.5.0,\)/'"$i"'/g' pom-$i.xml
done
