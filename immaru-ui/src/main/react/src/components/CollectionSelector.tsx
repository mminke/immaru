import React, { useState, useEffect } from 'react';
import { makeStyles } from '@material-ui/core/styles';
import Typography from '@material-ui/core/Typography';
import List from '@material-ui/core/List';
import ListItem from '@material-ui/core/ListItem';
import ListItemText from '@material-ui/core/ListItemText';
import ListItemIcon from '@material-ui/core/ListItemIcon';
import Paper from '@material-ui/core/Paper';
import Box from '@material-ui/core/Box';
import AddIcon from '@material-ui/icons/Add';
import BackgroundImage from '../images/background.jpg';
import { collectionRepository, Collection } from '../repositories/CollectionRepository'
import NewCollectionDialog from './NewCollectionDialog'

const useStyles = makeStyles((theme) => ({
    root: {
        backgroundImage: 'url(' + BackgroundImage + ')',
        backgroundRepeat: 'no-repeat',
        backgroundSize: 'cover',
    },
    titleContainer: {
        paddingBottom: "20px",
    },
    title: {
        fontFamily: "Pacifico",
        color: "white",
        opacity: "0.5",
        textShadow: "4px 4px black",
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

type Props = {
    onSelected: (collection: Collection) => void
}

export default function CollectionSelector({onSelected}: Props) {
    const classes = useStyles();

    const [collections, setCollections] = useState<Array<Collection>>()
    const [openNewCollectionDialog, setOpenNewCollectionDialog] = React.useState(false);

    useEffect( () => {
        collectionRepository.collections()
            .then(items => {
                setCollections(items)
            })
    }, [openNewCollectionDialog])

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
        setOpenNewCollectionDialog(false)
    }

    if(collections === undefined) return null;

    return <>
        <Box
            display="flex"
            justifyContent="center"
            alignItems="center"
            minHeight="100vh"
            className={classes.root}
        >

            <div>
                <div className={classes.titleContainer}>
                    <Typography variant="h1" className={classes.title} >Immaru</Typography>
                </div>

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
            </div>

            <NewCollectionDialog open={openNewCollectionDialog} onCreate={handleCollectionCreated} onClose={handleNewCollectionDialogClose}/>
        </Box>
    </>
}