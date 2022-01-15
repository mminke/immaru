import React, { useState, useEffect } from 'react';
import { makeStyles, createTheme, ThemeProvider } from '@material-ui/core/styles';
import CssBaseline from '@material-ui/core/CssBaseline';

import {BrowserRouter as Router, Routes, Route, Navigate} from "react-router-dom"
import {useHotkeys} from "react-hotkeys-hook"

import MainAppBar from './components/MainAppBar'
import MainDrawer from './components/MainDrawer'
import AssetList from './components/assetlist/AssetList'
import AssetDetails from './components/AssetDetails'
import AssetViewer from './components/AssetViewer'
import FileUpload from './components/FileUpload'
import CollectionSelector from './components/CollectionSelector'
import { Collection } from './repositories/CollectionRepository'
import AssetRepository, {Asset} from './repositories/AssetRepository'

import {hotkeysEnabledFilter} from './HotkeyState'

const useStyles = makeStyles((theme) => ({
  root: {
    display: 'flex',
    flexDirection: 'column',
    height: '100vh'
  },
  workspace: {
    display: 'flex',
    flexDirection: 'row',
    flexGrow: 1,
    overflow: 'hidden',
  },
  content: {
    display: 'flex',
    flexGrow: 1,
    overflowY: 'auto',
  },
  contentPadded: {
    display: 'flex',
    padding: theme.spacing(2),
    overflowX: 'hidden',
    flexGrow: 1,
  }
}));

export default function App() {
    const prefersDarkMode = true;
    const theme = React.useMemo(
        () =>
            createTheme({
                palette: {
                type: prefersDarkMode ? 'dark' : 'light',
                },
            }),
        [prefersDarkMode],
    );

    const classes = useStyles()

    const [mainDrawerOpen, setMainDrawerOpen] = useState(false);
    const toggleMainDrawer = () => {
        setMainDrawerOpen(!mainDrawerOpen);
    };

    const [activeCollection, setActiveCollection] = useState<Collection>()
    const handleCollectionSelected = (collection: Collection) => {
        setActiveCollection(collection)
    }

    const [assetDetailsOpen, setAssetDetailsOpen] = useState(false);

    const closeAssetDetails = () => {
        setAssetDetailsOpen(false)
    }

    const toggleAssetDetails = () => {
        setAssetDetailsOpen(!assetDetailsOpen)
    }

    const [selectedAsset, setSelectedAsset] = useState<Asset|null>(null);
    const handleImageSelected = (asset: Asset) => {
        setSelectedAsset(asset)
    }

    useHotkeys('i', hotkeysEnabledFilter(toggleAssetDetails), [assetDetailsOpen]);

    if(activeCollection === undefined) {
        return (
            <CollectionSelector onSelected={handleCollectionSelected}/>
        )
    } else {
        return (
            <ThemeProvider theme={theme}>
                <div className={classes.root}>
                    <CssBaseline/>
                    <Router>
                        <MainAppBar activeCollection={activeCollection} toggleMainDrawer={toggleMainDrawer}/>
                        <div className={classes.workspace}>
                            <MainDrawer open={mainDrawerOpen}/>
                            <AssetDetails activeCollection={activeCollection} asset={selectedAsset} open={assetDetailsOpen} onClose={closeAssetDetails}/>

                            <main className={classes.content}>
                                <div className={classes.contentPadded}>
                                    <Routes>
                                        <Route path="/upload" element={<FileUpload activeCollection={activeCollection}/>} />
                                        <Route path="/asset/:id" element={<AssetViewer collection={activeCollection}/>} />
                                        <Route path="/" element={<Navigate to="/media"/>} />
                                        <Route path="/media"
                                            element={<Media activeCollection={activeCollection} onImageSelected={handleImageSelected}/>}
                                        />
                                    </Routes>
                                </div>
                            </main>
                        </div>
                    </Router>
                </div>
            </ThemeProvider>
        );
  }
}


type MediaProps = {
    activeCollection: Collection,
    onImageSelected?: (asset: Asset ) => void
}

function Media({activeCollection, onImageSelected: handleImageSelected}: MediaProps) {
    return <>
        <div>
            <h1>test</h1>
        </div>
        <AssetList
            activeCollection={activeCollection}
            onImageSelected={handleImageSelected}
        />
    </>
}