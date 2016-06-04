#File Service

Camel Guice file service to process received files

To build the docker image using the maven plugin maven needs
a docker daemon to available.
Set maven runner environment variables e.g.
```
DOCKER_TLS_VERIFY=1
DOCKER_MACHINE_NAME=default
DOCKER_HOST=tcp://192.168.99.100:2376
DOCKER_CERT_PATH=/Users/user/.docker/machine/machines/default
```

Run the docker build using `mvn docker:build`
To run a container from the image `docker run -it --rm  margic/file-service`
