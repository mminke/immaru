import React, { useState, useEffect } from 'react';
import clsx from 'clsx';
import { makeStyles } from '@material-ui/core/styles';

import Drawer from '@material-ui/core/Drawer';
import Divider from '@material-ui/core/Divider';
import IconButton from '@material-ui/core/IconButton';
import ChevronLeftIcon from '@material-ui/icons/ChevronLeft';

import Table from '@material-ui/core/Table';
import TableBody from '@material-ui/core/TableBody';
import TableCell from '@material-ui/core/TableCell';
import TableContainer from '@material-ui/core/TableContainer';
import TableRow from '@material-ui/core/TableRow';
import Paper from '@material-ui/core/Paper';

import SelectTags from './SelectTags'

import AssetRepository, {Asset} from '../repositories/AssetRepository'
import TagRepository, {Tag} from '../repositories/TagRepository'
import {Collection} from '../repositories/CollectionRepository'

export const drawerWidth = 350;

const useStyles = makeStyles((theme) => ({
  drawerPaper: {
    position: 'relative',
    width: drawerWidth,
    padding: '10px',
    transition: theme.transitions.create('width', {
      easing: theme.transitions.easing.sharp,
      duration: theme.transitions.duration.enteringScreen,
    }),
  },
  drawerPaperClose: {
    overflowX: 'hidden',
    padding: '0px',
    transition: theme.transitions.create('width', {
      easing: theme.transitions.easing.sharp,
      duration: theme.transitions.duration.leavingScreen,
    }),
    width: 0,
  },
}));

type ImageDetailsProps = {
    activeCollection: Collection,
    asset: Asset|null,
    open: boolean,
    onClose: () => void
}

export default function ImageDetails({activeCollection, asset, open, onClose}: ImageDetailsProps) {
    const classes = useStyles();

    const tagRepository = new TagRepository()
    const assetRepository = new AssetRepository()

    const [tags, setTags] = useState<Tag[]>([])

    useEffect( () => {
        if(asset !== null) {
            Promise.all(
                asset.tagIds.map( (tagId: string) => {
                    var test = tagRepository.tagById(activeCollection.id, tagId)
                    return test
                })
            ).then( (tagsRetrieved: any) => {
                setTags(tagsRetrieved)
            })
        }
    }, [activeCollection, asset])

    const handleChangedTags = (tags: Tag[]) => {
        if(asset !== null) {
            asset.tagIds = tags.map( (tag) => tag.id )
            assetRepository.updateTagsFor(asset)
            setTags(tags)
        }
    }

    return <>
        <Drawer
            variant="permanent"
            anchor="left"
            open={open}
            classes={{
                paper: clsx(classes.drawerPaper, !open && classes.drawerPaperClose),
            }}
        >
            <div>
                <IconButton onClick={onClose}>
                    <ChevronLeftIcon />
                </IconButton>
            </div>
            <Divider />

            <h1>Image details</h1>

            <TableContainer component={Paper}>
                <Table size="small">
                    <TableBody>
                        <TableRow>
                            <TableCell>Filename</TableCell>
                            <TableCell>{asset !== null ? asset.originalFilename: ""}</TableCell>
                        </TableRow>
                        <TableRow>
                            <TableCell colSpan={2}>
                                <SelectTags selectedTags={tags} activeCollection={activeCollection} onChange={handleChangedTags}/>
                            </TableCell>
                        </TableRow>
                    </TableBody>
                </Table>
            </TableContainer>

        </Drawer>
    </>
}