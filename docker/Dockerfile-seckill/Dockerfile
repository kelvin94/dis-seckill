FROM openjdk:8-jdk-alpine as build

# Add Maintainer Info
LABEL maintainer="kelvinlingz@gmail.com"

# map local /tmp/seckill to container's /var/seckill, persist the data
VOLUME /tmp/seckill:/var/seckill
RUN apk update && apk add bash
RUN apk add curl && apk add jq
RUN mkdir -p /tmp/log/bash-scripts-logs/
ADD ./docker/Dockerfile-seckill/rabbitmq-init.sh /rabbitmq-init.sh
# Make port 8080 available to the world outside this container
EXPOSE 8083

# The application's jar file, The ARG instruction defines a variable with a default value. 
ARG JAR_FILE=target/secKill-0.0.1-SNAPSHOT.jar

# Add the application's jar to the container
ADD ${JAR_FILE} /var/seckill/seckill.jar

# Run the jar file
#CMD ["java","-Dspring.profiles.active=dev","-jar","/var/seckill/seckill.jar"]
#CMD ["/rabbitmq-init.sh"]

ADD ./docker/Dockerfile-seckill/start.sh /start.sh
CMD ["/start.sh"]
ENTRYPOINT ["/bin/bash"]
