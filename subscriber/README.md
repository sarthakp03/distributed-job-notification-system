# Subscriber Backend

This Java application is the backend server for the frontend Subscriber React application. It consists of REST API endpoints for the communication between frontend React application and broker Java application.

## Dependency management, build and deployment

### Dependency management
To manage third-party dependencies, Gradle has been integrated with this application. All the dependencies for this project are added as part of the file build.gradle

Commands used:
```
gradle clean build
gradle --refresh-dependencies
```

### Building the project
To JAR has been created which is stored at the build folder using the option Build > Build Project in IntelliJ

### Deployment
To quickly deploy and scale applications Docker has been used. The required settings has been provided in the Dockerfile. To fasten the process of pulling the docker images, the docker images have been published Docker Hub Container Image Library (https://hub.docker.com/)

This docker image is pulled on an AWS EC2 instance and run as a docker container. To communicate between two containers across different AWS EC2 instances, Public DNS URLs are used.

Commands used:
```
docker build -t sub-java-app .
docker tag sub-java-app pranav2306/subscriber-backend:latest
docker push pranav2306/subscriber-backend
```


