import React, { useState, useEffect } from 'react';
import { makeStyles } from '@material-ui/core/styles';
import { useParams, useNavigate } from "react-router-dom"
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

type Props = {
    collection: Collection
}

export default function AssetViewer({collection}: Props) {
    const {id} = useParams()
    const navigate = useNavigate()
    const classes = useStyles()

    const [asset, setAsset] = useState<Asset>()

    useEffect( () => {
        if(!!id) {
            assetRepository.assetWithId(collection, id)
                .then(assetRetrieved => {
                    setAsset(assetRetrieved)
                })
        }
    }, [collection, id])

    const closeAssetViewer = () => {
        navigate("/lightbox")
    }

    useHotkeys('v', (event:any) => {
        event.preventDefault()
        closeAssetViewer()
    });

    if(asset !== undefined && asset != null) {
        let url = `/collections/${collection.id}/assets/${asset.id}/content`

        if(asset.mediaType.startsWith("image/")) {
            return (
                <img src={url} alt={asset.originalFilename} className={classes.image}/>
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