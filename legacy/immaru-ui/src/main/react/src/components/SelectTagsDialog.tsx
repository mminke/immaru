import React, {useState} from 'react';
import Dialog from '@material-ui/core/Dialog';
import DialogActions from '@material-ui/core/DialogActions';
import DialogContent from '@material-ui/core/DialogContent';
import DialogContentText from '@material-ui/core/DialogContentText';
import DialogTitle from '@material-ui/core/DialogTitle';
import Button from '@material-ui/core/Button';

import TagSelector from './TagSelector'

import {enableHotkeys, disableHotkeys} from '../HotkeyState'

import {Collection} from '../repositories/CollectionRepository'
import {Tag} from '../repositories/TagRepository'

type Props = {
    activeCollection: Collection,
    open: boolean,
    onClose: () => void,
    onSelect: (selectedTags: Array<Tag>) => void
}

export default function SelectTagsDialog({
    activeCollection, open, onClose: handleClose, onSelect: handleSelect
}: Props ) {
    const [tags, setTags] = useState<Tag[]>([])

    const handleChangedTags = (tags: Tag[]) => {
        setTags(tags)
    }

    if(open) {
        disableHotkeys()
    } else {
        enableHotkeys()
    }

    const handleConfirm = () => {
        handleSelect(tags)
        setTags([])
    }

    return (
        <Dialog open={open} onClose={handleClose}>
            <DialogTitle id="form-dialog-title">Select tags</DialogTitle>
            <DialogContent>
                <DialogContentText>
                    Select the tags to use.
                </DialogContentText>

                <TagSelector selectedTags={tags} activeCollection={activeCollection} onChange={handleChangedTags} allowAddNewTags autoFocus/>
            </DialogContent>
            <DialogActions>
                <Button onClick={handleClose} color="primary">
                    Cancel
                </Button>
                <Button onClick={handleConfirm} color="primary">
                    Confirm
                </Button>
            </DialogActions>
        </Dialog>
  )
}