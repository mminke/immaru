import React, { useState, useEffect } from 'react';
import clsx from 'clsx';
import { makeStyles, useTheme } from '@material-ui/core/styles';
import List from '@material-ui/core/List';
import ListItem from '@material-ui/core/ListItem';
import ListItemText from '@material-ui/core/ListItemText';
import ListItemIcon from '@material-ui/core/ListItemIcon';
import Paper from '@material-ui/core/Paper';
import Box from '@material-ui/core/Box';
import AddIcon from '@material-ui/icons/Add';
import BackgroundImage from './images/background.jpg';

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

class CollectionRepository {

    static headers: HeadersInit = {'Accept': 'application/json'}

    async collections() {
        let collections = fetch('/collections', {headers: CollectionRepository.headers})
            .then(response => response.json())

        return collections
    }
}

type Collection = {
    id: string,
    name: string,
}

export default function CollectionSelector() {
    const classes = useStyles();
    const theme = useTheme();
    const collectionRepository = new CollectionRepository();

    const [collections, setCollections] = useState()
    const [activeCollection, setActiveCollection] = useState<Collection>()

    useEffect( () => {
        collectionRepository.collections()
            .then(items => {
                setCollections(items)
            })
    }, [])

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
                          <ListItem button>
                            <ListItemText primary={collection.name} secondary="Collection" key={collection.id}/>
                          </ListItem>
                    ))}

                    <ListItem button className={classes.addItem}>
                        <ListItemIcon className={classes.addItemIcon}>
                            <AddIcon />
                        </ListItemIcon>
                    </ListItem>
                </List>
            </Paper>
        </Box>
    </>
}