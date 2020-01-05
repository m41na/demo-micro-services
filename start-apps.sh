#!/usr/bin/env bash
echo "prepare to start all apps"
projects=( "demo-rest-service" "demo-repo-service")
port=7080
home=$(pwd)
echo "current home is $home"
for app in "${projects[@]}"
do
    echo "starting $app"
    cd "$app"
    bash ./run-app.sh -p $port -h localhost &
    port=$((port+1))
    cd "$home"
done

queue="demo-rest-service"
cd "$queue"
"$GRAALVM_HOME"/bin/java -cp deploy/demo-rest-service-0.0.1-dist.jar works.hop.rest.queue.QueApp &
cd "$home"

echo "apps should be ready now"
