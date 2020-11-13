import React from 'react';
import clsx from 'clsx';
import { makeStyles } from '@material-ui/core/styles';
import AppBar from '@material-ui/core/AppBar';
import Toolbar from '@material-ui/core/Toolbar';
import IconButton from '@material-ui/core/IconButton';
import Badge from '@material-ui/core/Badge';
import MenuIcon from '@material-ui/icons/Menu';
import NotificationsIcon from '@material-ui/icons/Notifications';
import Typography from '@material-ui/core/Typography';

import { Collection } from '../repositories/CollectionRepository'
import { drawerWidth } from './MainDrawer'

const useStyles = makeStyles((theme) => ({
    mainAppBar: {
        zIndex: theme.zIndex.drawer + 1,
    },
    menuButton: {
        marginRight: 36,
    },
    title: {
        flexGrow: 1,
    },
    toolbar: theme.mixins.toolbar,
}));

type MainAppBarProps = {
    activeCollection: Collection
    toggleMainDrawer: () => void
}

export default function MainAppBar({activeCollection, toggleMainDrawer}: MainAppBarProps) {
    const classes = useStyles();

    return <>
        <AppBar className={classes.mainAppBar}>
            <Toolbar>
                <IconButton
                    edge="start"
                    color="inherit"
                    aria-label="open drawer"
                    onClick={toggleMainDrawer}
                    className={classes.menuButton}
                >
                    <MenuIcon />
                </IconButton>
                <Typography component="h1" variant="h6" color="inherit" noWrap className={classes.title}>
                    {activeCollection.name}
                </Typography>
                <IconButton color="inherit">
                    <Badge badgeContent={4} color="secondary">
                        <NotificationsIcon />
                    </Badge>
                </IconButton>
            </Toolbar>
        </AppBar>
        <div className={classes.toolbar}/>
    </>
}