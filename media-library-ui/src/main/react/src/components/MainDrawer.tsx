import React from 'react';
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
import DashboardIcon from '@material-ui/icons/Dashboard';
import SaveAltIcon from '@material-ui/icons/SaveAlt';
import Toolbar from '@material-ui/core/Toolbar';
import {Link} from "react-router-dom"

export const drawerWidth = 240;

const useStyles = makeStyles((theme) => ({
  drawerPaper: {
    position: 'relative',
    whiteSpace: 'nowrap',
    width: drawerWidth,
    transition: theme.transitions.create('width', {
      easing: theme.transitions.easing.sharp,
      duration: theme.transitions.duration.enteringScreen,
    }),
  },
  drawerPaperClose: {
    overflowX: 'hidden',
    transition: theme.transitions.create('width', {
      easing: theme.transitions.easing.sharp,
      duration: theme.transitions.duration.leavingScreen,
    }),
    width: theme.spacing(7),
    [theme.breakpoints.up('sm')]: {
      width: theme.spacing(7),
    },
  },
}));

type MainDrawerProps = {
    open: boolean,
}

export default function MainDrawer({open}: MainDrawerProps) {
    const classes = useStyles();

    return <>
        <Drawer
            variant="permanent"
            open={open}
            classes={{
            paper: clsx(classes.drawerPaper, !open && classes.drawerPaperClose),
            }}
        >
            <Toolbar/>
            <List>{mainListItems}</List>
        </Drawer>
    </>
}

const mainListItems = (
    <>
        <ListItem button component={Link} to="/media">
            <ListItemIcon>
                <DashboardIcon />
            </ListItemIcon>
            <ListItemText primary="Media" />
        </ListItem>
        <ListItem button component={Link} to="/upload">
            <ListItemIcon>
                <SaveAltIcon />
            </ListItemIcon>
            <ListItemText primary="Upload" />
        </ListItem>
    </>
);