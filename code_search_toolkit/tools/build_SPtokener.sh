#!/bin/bash

if [ -z "$1" ]; then
  echo "Usage: $0 <worker_id>"
  exit 1
fi

WORKER_ID="$1"

cd ../SPtokener || { echo "ERROR: каталог SPtokener не найден"; exit 1; }

docker build --platform=amd64 -t sptokener-runner-"$WORKER_ID" .
if [ $? -ne 0 ]; then
  echo "ERROR: сборка образа sptokener-runner-$WORKER_ID завершилась неудачей"
  exit 1
fi

echo "INFO: SPtokener image успешно собран (sptokener-runner-$WORKER_ID)"