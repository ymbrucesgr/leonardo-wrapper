# leonardo-server

## Introduction
Leonardo-server is a backend server that wraps the interface to leonardo ai.

## Run server

### Run with docker (use image from dockerhub)
1. Run `docker pull ymbruce/leonardo-server:latest` to pull docker image.
2. Run `docker run -p 7010:7010 ymbruce/leonardo-server` to run server.

### Run with docker (build image by yourself)
1. Run `docker build -t ymbruce/leonardo-server .` to build a docker image.
2. Run `docker run -p 7010:7010 ymbruce/leonardo-server` to run server.

### Run with idea ide
1. Go to `org.generationai.Application` class, click the 'Run' button.

## Usage
1. Go to swagger `ip:port/api/swagger-ui.html`.
2. Send http request to api `ip:port/api/generateImage`.
3. Get image by api `ip:port/api/getImage`.