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
import {tagRepository, Tag} from '../repositories/TagRepository'

const useStyles = makeStyles((theme: Theme) =>
  createStyles({
    root: {
      '& > * + *': {
        marginTop: theme.spacing(3),
      },
    },
  }),
);

type TagFilterProps = {
    selectedTags: Tag[],
    activeCollection: Collection,
    onChange: (tags: Tag[]) => void
}

export default function TagFilter({selectedTags, activeCollection, onChange}: TagFilterProps) {
    const classes = useStyles();

    const [tags, setTags] = useState<Tag[]>([])

    const updateTags = (tags: Tag[]) => {
        onChange(tags)
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
            freeSolo
            renderTags={(value: any, getTagProps) =>
              value.map((tag: Tag, index: number) => (
                <Chip variant="outlined" label={tag.name} {...getTagProps({ index })} />
              ))
            }
            renderInput={(params) => (
              <TextField {...params} placeholder="Filter by tags"/>
            )}
            onChange={(event: any, elements: any[]) => {
                const changedTags: Tag[] = []
                elements.forEach( (element) => {
                    if( typeof element === 'string' ) {
                        var tagForPlainValue = tags.filter( (tag) => {
                            return tag.name.toLowerCase().trim() === element.toLowerCase().trim()
                        } )[0]

                        if( tagForPlainValue === undefined ) {
                        //
                        } else {
                            changedTags.push(tagForPlainValue)
                        }
                    } else {
                        changedTags.push(element)
                    }
                })

                updateTags(changedTags)
            }}
          />
        </div>
    );
}
