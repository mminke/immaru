# Earthrevealed Media Library

## Development

### Building

### Running React frontend

Start the postgres db:

    $ docker-compose up

Start the react frontend:

    $ cd media-library-ui/src/main/react
    $ yarn start    
    
## Usage

# Uploading files
 
To upload a single file using cUrl:

    $ curl -v -F files=@Untitled.png http://localhost:8080/assets

To upload multiple files using cUrl:

    $ curl -F 'files[]=@/path/to/fileX' -F 'files[]=@/path/to/fileY' ... http://localhost:8080/assets
