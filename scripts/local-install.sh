#!/usr/bin/env bash

mvn \
	-Dproject.parent.basedir=`pwd` \
	-Dproject.parent.parent.basedir=`pwd` \
	-Dproject.parent.parent.parent.basedir=`pwd` \
	-Dmaven.test.skip=true \
	-Drat.skip=true \
	-Dcheckstyle.skip \
  source:jar \
  install
