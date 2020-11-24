import React, { useState } from 'react';
import clsx from 'clsx';
import { makeStyles, useTheme } from '@material-ui/core/styles';
import {DropzoneArea} from 'material-ui-dropzone';
import SaveAltIcon from '@material-ui/icons/SaveAlt';
import Fab from '@material-ui/core/Fab'
import Zoom from '@material-ui/core/Zoom';
import AssetRepository from '../repositories/AssetRepository'
import {Collection} from '../repositories/CollectionRepository'

const useStyles = makeStyles((theme) => ({
  fab: {
    position: 'absolute',
    bottom: theme.spacing(2),
    right: theme.spacing(2),
    zIndex: 999,
  },
}));

type FileUploadProps = {
    activeCollection: Collection
}

export default function FileUpload({activeCollection}: FileUploadProps) {
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
        assetRepository.saveIn(activeCollection, files)
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