#!/bin/bash

java -jar /app/gateway.jar -config=/app/application-gateway.yaml &
java -jar /app/backend.jar -config=/app/application-backend.yaml &

wait -n
exit $?
