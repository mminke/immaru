# Earthrevealed Media Library

## Development

For development of the media library the following tools are required:

* docker
* docker-compose
* yarn

### Running React frontend

Step 1: Start the postgres db using docker compose development configuration.

    docker-compose up
    
Step 2: Run the media-library in your IDE.

Step 3: Start the react frontend.

    cd media-library-ui/src/main/react
    yarn start    

### Building the docker container

To build the docker container, use the build.sh script in the
'docker' directory.

    cd docker
    ./build.sh
    
## Running

To run the media-library the easiest way is the build the docker image
and then use docker-compose to startup the media-library with the necessary
postgres server.

    cd docker
    docker-compose up    
        
## Media Library Usage

### Uploading files
 
To upload a single file using cUrl:

    curl -v -F files=@Untitled.png http://localhost:8080/assets

To upload multiple files using cUrl:

    curl -F 'files[]=@/path/to/fileX' -F 'files[]=@/path/to/fileY' ... http://localhost:8080/assets
