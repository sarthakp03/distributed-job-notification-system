FROM openjdk:17
EXPOSE 8081
COPY build/libs/Broker-1.0-SNAPSHOT.jar broker.jar
ENTRYPOINT ["java", "-jar", "/broker.jar"]