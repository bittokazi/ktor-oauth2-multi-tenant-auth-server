#!/bin/bash

java -jar gateway.jar config=/app/application-gateway.yaml &
java -jar backend.jar config=/app/application-backend.yaml &

wait -n
exit $?
