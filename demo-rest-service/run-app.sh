#!/usr/bin/env bash
# java9+ -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005
# java8- -Xdebug -Xrunjdwp:transport=dt_socket,address=5005,server=y,suspend=y
"$GRAALVM_HOME"/bin/java -jar deploy/demo-rest-service-0.0.1-dist.jar com.practicaldime.zesty.app.AppLoader -p 7080 -h localhost -n demoRest -httpsPort 8443
