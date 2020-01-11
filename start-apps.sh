#!/usr/bin/env bash
echo "prepare to start all apps"
projects=("demo-rest-service" "demo-todo-service" "demo-repo-service")
port=7080
home=$(pwd)
echo "current home is $home"
for app in "${projects[@]}"; do
  echo "starting $app"
  cd "$app"
  bash ./launch.sh -p $port -h localhost &
  port=$((port + 1))
  cd "$home"
done

echo "apps should be ready now"
