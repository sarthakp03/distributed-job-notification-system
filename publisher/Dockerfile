FROM openjdk:11
EXPOSE 8080
COPY build/libs/test-1.0-SNAPSHOT.jar publisher.jar
ENTRYPOINT ["java", "-jar", "/publisher.jar"]
