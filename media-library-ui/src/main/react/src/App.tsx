import React, { useState } from 'react';
import { makeStyles } from '@material-ui/core/styles';
import Container from '@material-ui/core/Container';
import Toolbar from '@material-ui/core/Toolbar';
import Box from '@material-ui/core/Box';

import {BrowserRouter as Router, Switch, Route} from "react-router-dom"

import MainAppBar from './MainAppBar'
import MainDrawer from './MainDrawer'
import ImageList from './ImageList'
import ImageDetails from './ImageDetails'
import FileUpload from './FileUpload'
import CssBaseline from '@material-ui/core/CssBaseline';

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

  const [imageDetailsOpen, setImageDetailsOpen] = useState(false);
  const openImageDetails = () => {
    setImageDetailsOpen(true);
  }
  const closeImageDetails = () => {
    setImageDetailsOpen(false);
  }

  const [selectedAsset, setSelectedAsset] = useState(null);
  const handleImageSelected = (asset: any) => {

console.log(asset);
    setSelectedAsset(asset);
    openImageDetails();
  }

  return (
    <div className={classes.root}>
        <CssBaseline/>
        <Router>
            <MainAppBar toggleMainDrawer={toggleMainDrawer}/>
            <MainDrawer open={mainDrawerOpen}/>

            <ImageDetails asset={selectedAsset} open={imageDetailsOpen} onClose={closeImageDetails}/>

            <main className={classes.content}>
                <Toolbar/>
                <Switch>
                    <Route path="/upload">
                        <FileUpload/>
                    </Route>
                   <Route path={["/", "/media"]}>
                          <ImageList onImageSelected={handleImageSelected}/>
                    </Route>
                </Switch>
            </main>
        </Router>
    </div>
  );
}
