# Publisher Frontend

This React application is the frontend client for the backend Publisher Java application. It consists of REST API endpoints and pages for the communication to the Publisher Java application.

## Creation, Dependency management, build and deployment

### Creating the react app

```
npm create-react-app publisher-app
```

### Dependency management

To manage third-party dependencies, npm install was used.

For example:

```
npm install axios
```

### Run the application

```
npm start
```

### Deployment

To quickly deploy and scale applications Docker has been used. The required settings has been provided in the Dockerfile. To fasten the process of pulling the docker images, the docker images have been published Docker Hub Container Image Library (https://hub.docker.com/)

This docker image is pulled on an AWS EC2 instance and run as a docker container. To communicate between two containers across different AWS EC2 instances, Public DNS URLs are used.

Commands used:

```
docker build -t sarthakp263/publisher-front-end:latest
docker push sarthakp263/publisher-front-end:latest
```
