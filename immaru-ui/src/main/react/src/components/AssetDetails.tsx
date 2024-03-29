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

import TagSelector from './TagSelector'

import {assetRepository, Asset} from '../repositories/AssetRepository'
import {tagRepository, Tag} from '../repositories/TagRepository'
import {Collection} from '../repositories/CollectionRepository'
import {useHotkeys} from "react-hotkeys-hook"
import {hotkeysEnabledFilter} from '../HotkeyState'

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

type Props = {
    activeCollection: Collection,
    asset: Asset|null,
}



export default function AssetDetails({activeCollection, asset}: Props) {
    const classes = useStyles();

    const [open, setOpen] = useState<boolean>(false)

    const toggleAssetDetails = () => {
        setOpen(!open)
    }

    // The component should not know about the hotkey which triggers the open close?!?!??!
    useHotkeys('i', hotkeysEnabledFilter(toggleAssetDetails), [open]);

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
        {open&&asset&&<Drawer
            variant="permanent"
            anchor="left"
            open={true}
            classes={{
                paper: clsx(classes.drawerPaper, !open && classes.drawerPaperClose),
            }}
        >
            <div>
                <IconButton onClick={() => {setOpen(false)}}>
                    <ChevronLeftIcon />
                </IconButton>
            </div>
            <Divider />

            <h1>Details</h1>

            { asset !== null &&
                <TableContainer component={Paper}>
                    <Table size="small">
                        <TableBody>
                            <TableRow>
                                <TableCell>Filename</TableCell>
                                <TableCell>{asset.originalFilename}</TableCell>
                            </TableRow>
                            <TableRow>
                                <TableCell>Media type</TableCell>
                                <TableCell>{asset.mediaType}</TableCell>
                            </TableRow>
                            <TableRow>
                                <TableCell>Original creation date</TableCell>
                                <TableCell>{asset.originalCreatedAt ? asset.originalCreatedAt.replace("T", " "): ""}</TableCell>
                            </TableRow>
                            <TableRow>
                                <TableCell>Width</TableCell>
                                <TableCell>{asset.width + " px"}</TableCell>
                            </TableRow>
                            <TableRow>
                                <TableCell>Height</TableCell>
                                <TableCell>{asset.height + " px"}</TableCell>
                            </TableRow>
                            <TableRow>
                                <TableCell colSpan={2}>
                                    <TagSelector selectedTags={tags} activeCollection={activeCollection} onChange={handleChangedTags} allowAddNewTags/>
                                </TableCell>
                            </TableRow>
                        </TableBody>
                    </Table>
                </TableContainer>
            }
        </Drawer>
        }
    </>
}
