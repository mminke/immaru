import React, {useState} from 'react';
import Dialog from '@material-ui/core/Dialog';
import DialogActions from '@material-ui/core/DialogActions';
import DialogContent from '@material-ui/core/DialogContent';
import DialogContentText from '@material-ui/core/DialogContentText';
import DialogTitle from '@material-ui/core/DialogTitle';
import Button from '@material-ui/core/Button';
import TextField from '@material-ui/core/TextField';

import TagSelector from './TagSelector'

import {enableHotkeys, disableHotkeys} from '../HotkeyState'

import {Collection} from '../repositories/CollectionRepository'
import {Tag} from '../repositories/TagRepository'
import {Asset} from '../repositories/AssetRepository'

type Props = {
    activeCollection: Collection,
    assets: Array<Asset>,
    open: boolean,
    onClose: () => void,
}

export default function AddToGroupDialog(
    {activeCollection, assets, open, onClose: handleClose}: Props) {

    const [name, setName] = useState<string>("")

    if(open) {
        disableHotkeys()
    } else {
        enableHotkeys()
    }

    const handleNameChange = (event: any) => {
        setName(event.target.value);
    }

    const handleCancel = () => {
        handleClose()
    }

    const handleConfirm = () => {
        // Make call to add the assets to the group

        alert('Group name: ' + name)
        //handleSelect()
    }

    return (
        <Dialog open={open} onClose={handleClose}>
            <DialogTitle id="form-dialog-title">Add to group</DialogTitle>
            <DialogContent>
                <DialogContentText>
                    Add the selected assets to a group.
                </DialogContentText>

                <label>
                    <TextField placeholder="Name" value={name} onChange={handleNameChange} />
                </label>
            </DialogContent>
            <DialogActions>
                <Button onClick={handleCancel} color="primary">
                    Cancel
                </Button>
                <Button onClick={handleConfirm} color="primary">
                    Confirm
                </Button>
            </DialogActions>
        </Dialog>
  )
}