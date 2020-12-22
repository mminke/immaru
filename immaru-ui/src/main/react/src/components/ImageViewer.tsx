import React, { useState, useEffect } from 'react';
import { makeStyles } from '@material-ui/core/styles';
import { useParams } from "react-router-dom"
import { Collection } from '../repositories/CollectionRepository'
import AssetRepository, {Asset} from '../repositories/AssetRepository'

type ImageViewerProps = {
    collection: Collection
}

export default function ImageViewer({collection}: ImageViewerProps) {
    const {id} = useParams()
    const assetRepository = new AssetRepository();

    const [asset, setAsset] = useState<Asset>()
    let url = ''
    if(asset !== undefined) {
        url = `/collections/${collection.id}/assets/${asset.id}`
    }
    const style = {
        width: '100%',
        height: '100%',
        backgroundSize: 'contain',
        backgroundPosition: 'center center',
        backgroundRepeat: 'no-repeat',
        backgroundImage: `url(${url})`
    }

    useEffect( () => {
        assetRepository.assetWithId(collection, id)
            .then(assetRetrieved => {
                setAsset(assetRetrieved)
            })
    }, [collection])


    return (
        <div style={style}/>
    )
}