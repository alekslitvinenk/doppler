#!/usr/bin/env bash

# -v /data/logs/doppler:/var/log \

docker run \
-p 80:8080 \
--rm \
-e APP_LOG_APPENDER=console \
-e DB_HOST=172.17.0.2 \
alekslitvinenk/doppler \
 0.0.0.0 8080