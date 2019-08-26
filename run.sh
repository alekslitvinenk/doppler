#!/usr/bin/env bash

# -v /data/logs/doppler:/var/log \

docker run -d \
-p 80:8080 \
--rm \
-e APP_LOG_APPENDER=rollingFile \
-e DB_HOST=167.71.114.232 \
alekslitvinenk/doppler \
 0.0.0.0 8080