#!/bin/bash
set -ev
if [$PROJECT="redis"]
then
  cd util && sbt +publish-local && cd ../$PROJECT && sbt test
else
  cd util && sbt +publish-local && cd ../$PROJECT && sbt +test
fi
