import React, {useState} from 'react';
import Button from '@material-ui/core/Button';
import TextField from '@material-ui/core/TextField';
import Dialog from '@material-ui/core/Dialog';
import DialogActions from '@material-ui/core/DialogActions';
import DialogContent from '@material-ui/core/DialogContent';
import DialogContentText from '@material-ui/core/DialogContentText';
import DialogTitle from '@material-ui/core/DialogTitle';
import {collectionRepository} from '../repositories/CollectionRepository';

type Props = {
    open: boolean,
    onClose: () => void
    onCreate: () => void
}

export default function NewCollectionDialog({open, onClose, onCreate}: Props) {

    const [name, setName] = useState<String>("")

    const handleCreate = () => {
        if(!!name && name.length > 0) {
            collectionRepository
                .create({ "name": name })
                .then(() => onCreate())
            setName("")
        }
    }

    const handleChange = (event: React.ChangeEvent<HTMLInputElement>) => {
        setName(event.target.value);
    };

    return (
      <Dialog open={open} onClose={onClose} aria-labelledby="form-dialog-title">
        <DialogTitle id="form-dialog-title">New collection</DialogTitle>
        <DialogContent>
          <DialogContentText>
            Create a new collection to organize media in.
          </DialogContentText>
          <TextField
            autoFocus
            margin="dense"
            id="name"
            label="Name"
            fullWidth
            value={name}
            onChange={handleChange}
          />
        </DialogContent>
        <DialogActions>
          <Button onClick={onClose} color="primary">
            Cancel
          </Button>
          <Button onClick={handleCreate} color="primary">
            Create
          </Button>
        </DialogActions>
      </Dialog>
  );
}