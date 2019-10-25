# 💧 Doppler
[![Build Status](https://travis-ci.org/alekslitvinenk/doppler.svg?branch=master)](https://travis-ci.org/alekslitvinenk/doppler)
[![Scala Steward badge](https://img.shields.io/badge/Scala_Steward-helping-blue.svg?style=flat&logo=data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAA4AAAAQCAMAAAARSr4IAAAAVFBMVEUAAACHjojlOy5NWlrKzcYRKjGFjIbp293YycuLa3pYY2LSqql4f3pCUFTgSjNodYRmcXUsPD/NTTbjRS+2jomhgnzNc223cGvZS0HaSD0XLjbaSjElhIr+AAAAAXRSTlMAQObYZgAAAHlJREFUCNdNyosOwyAIhWHAQS1Vt7a77/3fcxxdmv0xwmckutAR1nkm4ggbyEcg/wWmlGLDAA3oL50xi6fk5ffZ3E2E3QfZDCcCN2YtbEWZt+Drc6u6rlqv7Uk0LdKqqr5rk2UCRXOk0vmQKGfc94nOJyQjouF9H/wCc9gECEYfONoAAAAASUVORK5CYII=)](https://scala-steward.org)

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
