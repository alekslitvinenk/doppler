FROM openjdk:17.0-jdk-oracle
COPY /target/scala-2.13 /usr/src/myapp
WORKDIR /usr/src/myapp
ENTRYPOINT [ "java", "-jar", "doppler-assembly-0.1.jar" ]
CMD [ "$@" ]