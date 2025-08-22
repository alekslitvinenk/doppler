FROM alpine:3.19

LABEL maintainer="Alexander Litvinenko <array.shift@yahoo.com>"

# System settings. User normally shouldn't change these parameters
ENV APP_NAME=GitClone
ENV APP_INSTALL_PATH=/opt/${APP_NAME}

WORKDIR ${APP_INSTALL_PATH}

# Exposed config
ENV GIT_REPO="https://github.com/dockovpn/dockovpn.io-web-content.git"
ENV GIT_COMMIT="HEAD"
ENV DESTINATION_DIR="/var/www/hosts"

COPY scripts .

RUN apk add --no-cache bash git dumb-init

ENTRYPOINT [ "dumb-init", "./start.sh" ]
CMD [ "" ]



