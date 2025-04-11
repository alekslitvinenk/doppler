# 💧 Doppler

## To run
```bash
docker run -d \
-p 80:8080 \
-p 443:9443 \
--rm \
-v /opt/doppler:/var/www/hosts \
-v /etc/letsencrypt/live/dockovpn.io-0001:/opt/sslfiles \
-e APP_LOG_APPENDER=rollingFile \
-e DB_HOST=??? \
-e SSL_CERT=/opt/sslfiles/server.p12 \
alekslitvinenk/doppler:edge2 \
0.0.0.0
```

## To convert SSL/TLS files obtained from Certbot
1. Navigate to unzipped folder with certificates, usually `/etc/letsencrypt/live/dockovpn.io`
2. `openssl pkcs12 -export -out server.p12 -inkey privkey.pem -in fullchain.pem -certfile cert.pem`
