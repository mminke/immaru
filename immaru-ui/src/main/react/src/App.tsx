import React, { useState, useEffect } from 'react';
import { makeStyles, createTheme, ThemeProvider } from '@material-ui/core/styles';
import CssBaseline from '@material-ui/core/CssBaseline';

import {BrowserRouter as Router, Routes, Route, Navigate} from "react-router-dom"
import {useHotkeys} from "react-hotkeys-hook"

import Autocomplete from '@material-ui/lab/Autocomplete';

import MainAppBar from './components/MainAppBar'
import MainDrawer from './components/MainDrawer'
import AssetList from './components/assetlist/AssetList'
import AssetDetails from './components/AssetDetails'
import AssetViewer from './components/AssetViewer'
import FileUpload from './components/FileUpload'
import TagSelector from './components/TagSelector'
import CollectionSelector from './components/CollectionSelector'
import { Collection } from './repositories/CollectionRepository'
import {assetRepository, Asset} from './repositories/AssetRepository'
import {Tag} from './repositories/TagRepository'

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
    flexDirection: 'column',
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

    const [selectedAsset, setSelectedAsset] = useState<Asset|null>(null);
    const handleImageSelected = (asset: Asset) => {
        setSelectedAsset(asset)
    }

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
                            <AssetDetails activeCollection={activeCollection} asset={selectedAsset}/>

                            <main className={classes.content}>
                                <div className={classes.contentPadded}>
                                    <Routes>
                                        <Route path="/upload" element={<FileUpload activeCollection={activeCollection}/>} />
                                        <Route path="/asset/:id" element={<AssetViewer collection={activeCollection}/>} />
                                        <Route path="/" element={<Navigate to="/lightbox"/>} />
                                        <Route path="/lightbox"
                                            element={<LightBox activeCollection={activeCollection} onImageSelected={handleImageSelected}/>}
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


type LightBoxProps = {
    activeCollection: Collection,
    onImageSelected?: (asset: Asset ) => void
}

function LightBox({activeCollection, onImageSelected: handleImageSelected}: LightBoxProps) {
    const [assets, setAssets] = useState<Array<Asset>>([])
    const [filterTags, setFilterTags] = useState<Array<Tag>>([])

    useEffect( () => {
        assetRepository.assetsFor(activeCollection, filterTags)
            .then(assetsRetrieved => {
                setAssets(assetsRetrieved)
            })
    }, [activeCollection, filterTags])

    const handleChangedFilterTags = (tags: Tag[]) => {
        setFilterTags(tags)
    }

    return <>
        <TagSelector
            selectedTags={filterTags}
            activeCollection={activeCollection}
            onChange={handleChangedFilterTags}
        />
        <AssetList
            activeCollection={activeCollection}
            assets={assets}
            onImageSelected={handleImageSelected}
        />
    </>
}