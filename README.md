# 💧 Doppler

## To convert SSL/TLS files obtained from Let's Encrypt Authority
1. Navigate to unzipped folder with certificates
2. `openssl pkcs12 -export -out server.p12 -inkey private.key -in certificate.crt -certfile ca_bundle.crt`