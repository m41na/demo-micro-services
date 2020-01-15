#!/usr/bin/env bash
# java9+ -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005
# java8- -Xdebug -Xrunjdwp:transport=dt_socket,address=5005,server=y,suspend=y
if [ -n "$GRAALVM_HOME" ]; then
  JAVA_HOME=$GRAALVM_HOME
fi
"$JAVA_HOME"/bin/java -jar deploy/demo-web-acceptor-0.0.1-dist.jar com.practicaldime.zesty.app.AppLoader -p 7081 -h localhost -n acceptorApp -assets www -natsHost localhost -natsPort 4222 -maxReconnect -1 -c app-config.properties
