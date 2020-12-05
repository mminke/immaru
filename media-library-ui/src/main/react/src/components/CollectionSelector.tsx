import React, { useState, useEffect } from 'react';
import { makeStyles } from '@material-ui/core/styles';
import List from '@material-ui/core/List';
import ListItem from '@material-ui/core/ListItem';
import ListItemText from '@material-ui/core/ListItemText';
import ListItemIcon from '@material-ui/core/ListItemIcon';
import Paper from '@material-ui/core/Paper';
import Box from '@material-ui/core/Box';
import AddIcon from '@material-ui/icons/Add';
import BackgroundImage from '../images/background.jpg';
import CollectionRepository, { Collection } from '../repositories/CollectionRepository'
import NewCollectionDialog from './NewCollectionDialog'

const useStyles = makeStyles((theme) => ({
    root: {
        backgroundImage: 'url(' + BackgroundImage + ')',
        backgroundRepeat: 'no-repeat',
        backgroundSize: 'cover',
    },
    collectionContainer: {
        minWidth: '200px',
    },
    addItem: {
        justifyContent: 'center',
    },
    addItemIcon: {
        minWidth: '0px',
    }
}));

type CollectionSelectorProps = {
    onSelected: (collection: Collection) => void
}

export default function CollectionSelector({onSelected}: CollectionSelectorProps) {
    const classes = useStyles();
    const collectionRepository = new CollectionRepository();

    const [collections, setCollections] = useState()

    const [reloadCollections, setReloadCollections] = useState(0)

    useEffect( () => {
        collectionRepository.collections()
            .then(items => {
                setCollections(items)
            })
    }, [reloadCollections])

    const handleCollectionSelected = (collection: Collection) => {
        onSelected(collection)
    }

    const addNewCollection = () => {
        setOpenNewCollectionDialog(true)
    }
    const handleNewCollectionDialogClose = () => {
        setOpenNewCollectionDialog(false)
    }
    const handleCollectionCreated = () => {
        setReloadCollections(reloadCollections + 1)
        setOpenNewCollectionDialog(false)
    }

    const [openNewCollectionDialog, setOpenNewCollectionDialog] = React.useState(false);

    if(collections === undefined) return null;

    return <>
        <Box
            display="flex"
            justifyContent="center"
            alignItems="center"
            minHeight="100vh"
            className={classes.root}
        >
            <Paper className={classes.collectionContainer}>
                <List component="nav" aria-label="Photo collections">
                    { collections.map( (collection: Collection) => (
                          <ListItem button onClick={() => handleCollectionSelected(collection)} key={collection.id}>
                            <ListItemText primary={collection.name} secondary="Collection"/>
                          </ListItem>
                    ))}

                    <ListItem button className={classes.addItem} onClick={addNewCollection} key="new_collection">
                        <ListItemIcon className={classes.addItemIcon}>
                            <AddIcon />
                        </ListItemIcon>
                    </ListItem>
                </List>
            </Paper>
            <NewCollectionDialog open={openNewCollectionDialog} onCreate={handleCollectionCreated} onClose={handleNewCollectionDialogClose}/>
        </Box>
    </>
}