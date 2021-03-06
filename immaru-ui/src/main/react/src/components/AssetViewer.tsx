import React, { useState, useEffect } from 'react';
import { makeStyles } from '@material-ui/core/styles';
import { useParams, useHistory } from "react-router-dom"
import {useHotkeys} from "react-hotkeys-hook"

import { Player } from 'video-react';

import { Collection } from '../repositories/CollectionRepository'
import {assetRepository, Asset} from '../repositories/AssetRepository'

import "video-react/dist/video-react.css";

const useStyles = makeStyles((theme) => ({
    image: {
        width: '100%',
        height: '100%',
        objectFit: 'contain',
    }
}));

type AssetViewerProps = {
    collection: Collection
}

export default function AssetViewer({collection}: AssetViewerProps) {
    const {id} = useParams()
    const history = useHistory()
    const classes = useStyles()

    const [asset, setAsset] = useState<Asset>()

    useEffect( () => {
        assetRepository.assetWithId(collection, id)
            .then(assetRetrieved => {
                setAsset(assetRetrieved)
            })
    }, [collection])

    const closeAssetViewer = () => {
        history.push("/media")
    }

    useHotkeys('v', (event:any) => closeAssetViewer());

    if(asset !== undefined) {
        let url = `/collections/${collection.id}/assets/${asset.id}`

        if(asset.mediaType.startsWith("image/")) {
            return (
                <img src={url} className={classes.image}/>
            )
        } else if (asset.mediaType.startsWith("video/")) {
            return (
                <Player
                      playsInline
                      src={url}
                    />
            )
        } else {
            return <div>Media type not supported</div>
        }
    }

    return <div></div>
}