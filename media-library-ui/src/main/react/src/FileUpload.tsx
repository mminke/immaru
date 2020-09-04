import React, { useState } from 'react';
import clsx from 'clsx';
import { makeStyles } from '@material-ui/core/styles';
import {DropzoneArea} from 'material-ui-dropzone';

export default function FileUpload() {

  const [files, setFiles] = useState([])

  const handleChange = (files: any) => {
    console.log('Files:', files)
    setFiles(files);
  }

    return <>
      <h1>Upload files</h1>
      <DropzoneArea
        onChange={handleChange}
      />
    </>
}