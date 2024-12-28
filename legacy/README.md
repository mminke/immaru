# Immaru Media Library

## Project setup

Two maven profiles: 
* dev [default]
* prod

Springboot config files:
* application.yml
* application-dev.yml [activated using the dev maven profile]

## Development

For development of Immaru the following tools are required:

* docker
* docker-compose
* yarn

### Configuration

Configuration of the database and location where media files are stored can be found here:

    immaru-app/src/main/resources/

For development purposes the media files are stored in:

    ${java.io.tmpdir}/data/immaru-dev

### Running the backend and frontend

Step 1: Start the postgres db using docker compose development configuration.

    docker-compose up
    
Step 2: Run the Immaru backend in your IDE.

    Run immaru-app/src/main/kotlin/com.earthrevealed.immaru.Application.main

Step 3: Start the react frontend.

    cd immaru-ui/src/main/react
    yarn start    


## Running Locally

To run Immaru the easiest way is to build the docker image
and then use docker-compose to startup Immaru with the necessary
postgres server.


### Building the docker container

To build the docker container, use the build.sh script in the
'docker' directory.

    cd docker
    ./build.sh

### Running Immaru locally

    cd docker
    docker-compose up

### Build production image

    mvn clean package -P prod
        
## Immaru Usage

Immaru has a React frontend which can be used to view and upload media files. 
It is also possible to directly use the rest backend calls to interact with Immaru.

### Uploading files using the rest backend
 
To upload a single file using cUrl:

    curl -v -F files=@Untitled.png http://localhost:8080/collections/{collection-id}/assets

To upload multiple files using cUrl:

    curl -F 'files[]=@/path/to/fileX' -F 'files[]=@/path/to/fileY' ... http://localhost:8080/collections/{collection-id}/assets
