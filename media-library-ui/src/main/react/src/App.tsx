import React, { useState } from 'react';
import { makeStyles } from '@material-ui/core/styles';
import Container from '@material-ui/core/Container';
import Toolbar from '@material-ui/core/Toolbar';
import Box from '@material-ui/core/Box';
import CssBaseline from '@material-ui/core/CssBaseline';

import {BrowserRouter as Router, Switch, Route} from "react-router-dom"

import MainAppBar from './components/MainAppBar'
import MainDrawer from './components/MainDrawer'
import ImageList from './components/ImageList'
import ImageDetails from './components/ImageDetails'
import FileUpload from './components/FileUpload'
import CollectionSelector from './components/CollectionSelector'
import { Collection } from './repositories/CollectionRepository'

const useStyles = makeStyles((theme) => ({
  root: {
    display: 'flex',
    height: '100vh'
  },
  content: {
    flexGrow: 1,
    overflow: 'auto',
    padding: theme.spacing(2),
  },
}));

export default function App() {
    const classes = useStyles();
    const [mainDrawerOpen, setMainDrawerOpen] = useState(false);
    const toggleMainDrawer = () => {
        setMainDrawerOpen(!mainDrawerOpen);
    };

    const [activeCollection, setActiveCollection] = useState<Collection>()
    const handleCollectionSelected = (collection: Collection) => {
        setActiveCollection(collection)
    }

    const [imageDetailsOpen, setImageDetailsOpen] = useState(false);
    const openImageDetails = () => {
        setImageDetailsOpen(true);
    }
    const closeImageDetails = () => {
        setImageDetailsOpen(false);
    }

    const [selectedAsset, setSelectedAsset] = useState(null);
    const handleImageSelected = (asset: any) => {
        setSelectedAsset(asset);
        openImageDetails();
    }

    if(activeCollection === undefined) {
        return (
            <CollectionSelector onSelected={handleCollectionSelected}/>
        )
    } else {
        return (
            <div className={classes.root}>
                <CssBaseline/>
                <Router>
                    <MainAppBar activeCollection={activeCollection} toggleMainDrawer={toggleMainDrawer}/>
                    <MainDrawer open={mainDrawerOpen}/>
                    <ImageDetails asset={selectedAsset} open={imageDetailsOpen} onClose={closeImageDetails}/>

                    <main className={classes.content}>
                        <Toolbar/>
                        <Switch>
                            <Route path="/upload">
                                <FileUpload activeCollection={activeCollection}/>
                            </Route>
                           <Route path={["/", "/media"]}>
                                  <ImageList activeCollection={activeCollection} onImageSelected={handleImageSelected}/>
                            </Route>
                        </Switch>
                    </main>
                </Router>
            </div>
        );
  }
}
