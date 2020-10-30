import React, { useState, useEffect } from 'react';
import clsx from 'clsx';
import { makeStyles } from '@material-ui/core/styles';

import Drawer from '@material-ui/core/Drawer';
import List from '@material-ui/core/List';
import Divider from '@material-ui/core/Divider';
import IconButton from '@material-ui/core/IconButton';
import ChevronLeftIcon from '@material-ui/icons/ChevronLeft';

import ListItem from '@material-ui/core/ListItem';
import ListItemIcon from '@material-ui/core/ListItemIcon';
import ListItemText from '@material-ui/core/ListItemText';
import Toolbar from '@material-ui/core/Toolbar';
import Chip from '@material-ui/core/Chip';
import DashboardIcon from '@material-ui/icons/Dashboard';
import SaveAltIcon from '@material-ui/icons/SaveAlt';
import {Link} from "react-router-dom"

import Table from '@material-ui/core/Table';
import TableBody from '@material-ui/core/TableBody';
import TableCell from '@material-ui/core/TableCell';
import TableContainer from '@material-ui/core/TableContainer';
import TableHead from '@material-ui/core/TableHead';
import TableRow from '@material-ui/core/TableRow';
import Paper from '@material-ui/core/Paper';

import SelectTags from './SelectTags'

import {Asset} from '../repositories/AssetRepository'
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
    asset: any,
    open: boolean,
    onClose: () => void
}

export default function ImageDetails({activeCollection, asset, open, onClose}: ImageDetailsProps) {
    const classes = useStyles();

    const tagRepository = new TagRepository()

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
    }, [asset])

    return <>
        <Drawer
            variant="permanent"
            anchor="left"
            open={open}
            classes={{
                paper: clsx(classes.drawerPaper, !open && classes.drawerPaperClose),
            }}
        >
            <Toolbar/>
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
                                <SelectTags activeCollection={activeCollection}/>
                            </TableCell>
                        </TableRow>
                    </TableBody>
                </Table>
            </TableContainer>

        </Drawer>
    </>
}
