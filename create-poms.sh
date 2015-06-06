#!/usr/bin/env bash

set -eux

cp pom.xml pom-1.7.xml
cp pom.xml pom-1.5.xml

perl -i -pe 's/1.6.0/1.7.0-RC1/g' pom-1.7.xml
perl -i -pe 's/1.6.0/1.5.1/g' pom-1.5.xml
