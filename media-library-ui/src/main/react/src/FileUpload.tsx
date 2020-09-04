import React, { useState } from 'react';
import clsx from 'clsx';
import { makeStyles, useTheme } from '@material-ui/core/styles';
import {DropzoneArea} from 'material-ui-dropzone';
import SaveAltIcon from '@material-ui/icons/SaveAlt';
import Fab from '@material-ui/core/Fab'
import Zoom from '@material-ui/core/Zoom';

const useStyles = makeStyles((theme) => ({
  fab: {
    position: 'absolute',
    bottom: theme.spacing(2),
    right: theme.spacing(2),
  }
}));


class AssetRepository {

    static headers: HeadersInit = {'Accept': 'application/json'}

    async save(files: File[]) {
        const formData = new FormData()

        files.forEach( (file: File) => {
            console.log('File:', file)
            formData.append(
                "files",
                file,
                file.name
            )
        })

        console.log('formData: ', formData)

        let result = fetch('/assets', {
                method: 'POST',
                headers: AssetRepository.headers,
                body: formData
            })
            .then(response => response.json())
            .catch(error => {
                console.error('Error uploading files:', error)
            })

        console.log('Successfully uploaded files: ', result)
    }
}

export default function FileUpload() {
    const classes = useStyles();
    const theme = useTheme();
    const assetRepository = new AssetRepository();

    const [files, setFiles] = useState<File[]>([])
    const [reloadKey, setReloadKey] = useState(0)

    const handleChange = (files: File[]) => {
        console.log('Files:', files)
        setFiles(files)
    }

    const handleUploadFiles = () =>  {
        assetRepository.save(files)
        setFiles([])
        setReloadKey(reloadKey+1)
    }

    return <>
      <h1>Upload files</h1>
      <Zoom
        in={files.length > 0}>
        <Fab color="primary" aria-label="Upload" onClick={handleUploadFiles} className={classes.fab}>
            <SaveAltIcon />
        </Fab>
      </Zoom>
      <DropzoneArea
        key={reloadKey}
        filesLimit={999}
        maxFileSize={999999999}
        onChange={handleChange}
      />
    </>
}