import React, {useState, useEffect} from 'react';
import Chip from '@material-ui/core/Chip';
import Autocomplete from '@material-ui/lab/Autocomplete';
import { createStyles, makeStyles, Theme } from '@material-ui/core/styles';
import TextField from '@material-ui/core/TextField';

import Dialog from '@material-ui/core/Dialog';
import DialogTitle from '@material-ui/core/DialogTitle';
import DialogContent from '@material-ui/core/DialogContent';
import DialogContentText from '@material-ui/core/DialogContentText';
import DialogActions from '@material-ui/core/DialogActions';
import Button from '@material-ui/core/Button';

import {Collection} from '../repositories/CollectionRepository'
import TagRepository, {Tag} from '../repositories/TagRepository'

const useStyles = makeStyles((theme: Theme) =>
  createStyles({
    root: {
      '& > * + *': {
        marginTop: theme.spacing(3),
      },
    },
  }),
);

type ImageListProps = {
    activeCollection: Collection
}

export default function SelectTags({activeCollection}: ImageListProps) {
    const tagRepository = new TagRepository()

    const classes = useStyles();

    const [tags, setTags] = useState<Tag[]>([])
    const [selectedTags, setSelectedTags] = useState<Tag[]>([])

    const [openNewTagDialog, toggleOpenNewTagDialog] = useState(false)
    const [newTagDialogValue, setNewTagDialogValue] = useState<string>("")

    const handleNewTagDialogSubmit = () => {
        tagRepository.create(activeCollection.id, { name: newTagDialogValue })
            .then( (newTags: any) => {
                if(newTags !== null) {
                    const editableTags = [...selectedTags]
                    newTags.forEach( (newTag: Tag) => { editableTags.push(newTag) } )
                    setSelectedTags(editableTags)
                }

                loadTags()
                setNewTagDialogValue("")
                toggleOpenNewTagDialog(false)
            })
    }
    const handleCloseNewTagDialog = () => {
        toggleOpenNewTagDialog(false)
    }

    useEffect( () => {
        loadTags()
    }, [])

    const loadTags = () => {
        return tagRepository.tags(activeCollection.id)
            .then(tagsRetrieved => {
                setTags(tagsRetrieved)
            })
    }

    return (
        <div className={classes.root}>
          <Autocomplete
            multiple
            id="tags-filled"
            options={tags}
            getOptionLabel={(tag) => {
                return tag.name
            }}
            value={selectedTags}
            defaultValue={[]}
            freeSolo
            renderTags={(value: any, getTagProps) =>
              value.map((tag: Tag, index: number) => (
                <Chip variant="outlined" label={tag.name} {...getTagProps({ index })} />
              ))
            }
            renderInput={(params) => (
              <TextField {...params} placeholder="Add tags"/>
            )}
            onChange={(event: any, elements: any[]) => {
                const changedTags: Tag[] = []
                elements.forEach( (element) => {
                    if( typeof element === 'string' ) {
                        var tagForPlainValue = tags.filter( (tag) => {
                            return tag.name.toLowerCase().trim() == element.toLowerCase().trim()
                        } )[0]

                        if( tagForPlainValue === undefined ) {
                            setNewTagDialogValue(element)
                            toggleOpenNewTagDialog(true)
                        } else {
                            changedTags.push(tagForPlainValue)
                        }
                    } else {
                        changedTags.push(element)
                    }
                })

                setSelectedTags(changedTags)
            }}
          />

          <Dialog open={openNewTagDialog} onClose={handleCloseNewTagDialog}>
              <DialogTitle id="form-dialog-title">Add a new tag</DialogTitle>
              <DialogContent>
                <DialogContentText>
                  Do you want to add this new tag
                </DialogContentText>
                <TextField
                  autoFocus
                  margin="dense"
                  id="name"
                  value={newTagDialogValue}
                  onChange={(event) => setNewTagDialogValue(event.target.value)}
                  label="name"
                  type="text"
                />
              </DialogContent>
              <DialogActions>
                <Button onClick={handleCloseNewTagDialog} color="primary">
                  Cancel
                </Button>
                <Button onClick={handleNewTagDialogSubmit} color="primary">
                  Add
                </Button>
              </DialogActions>
          </Dialog>

        </div>
    );
}
