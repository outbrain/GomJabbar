#!/bin/sh

main() {

  local log4jfile=$(pwd)/log4j.xml
  export MAVEN_OPTS="-Xmx1024m -Xms1024m -Dlog4j.configuration=file://$log4jfile $GJ_OPTS"
  mvn exec:java -Dexec.mainClass="com.outbrain.gomjabbar.GomJabbarServer"
}

main