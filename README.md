# 💧 Doppler
[![Build Status](https://travis-ci.org/alekslitvinenk/doppler.svg?branch=master)](https://travis-ci.org/alekslitvinenk/doppler)

## To run
```bash
docker run -d \
-p 80:8080 \
-p 443:9443 \
--rm \
-v /opt/doppler:/var/www/hosts \
alekslitvinenk/doppler \
 0.0.0.0
```

## To convert SSL/TLS files obtained from Let's Encrypt Authority
1. Navigate to unzipped folder with certificates
2. `openssl pkcs12 -export -out server.p12 -inkey private.key -in certificate.crt -certfile ca_bundle.crt`
