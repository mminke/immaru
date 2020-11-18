import React, { useState, useEffect } from 'react';
import { makeStyles } from '@material-ui/core/styles';
import CircularProgress from '@material-ui/core/CircularProgress';
import Typography from '@material-ui/core/Typography';

import AssetRepository, {Asset} from '../repositories/AssetRepository'
import {Collection} from '../repositories/CollectionRepository'


const useStyles = makeStyles((theme) => ({
    loader: {
        display: 'flex',
        flexGrow: 1,
        alignItems: 'center',
        justifyContent: 'center',
        height: '100%'
    },

    root: {
    },

    imageGridItem: {
        display: 'inline-block',
        boxSizing: 'border-box',
        float: 'left',
        padding: '10px',
        overflow: 'hidden',
        position: 'relative',
        transition: 'all .5s linear',
        transitionDelay: '.1s',
        '&:hover $imageWrapper': {
            backgroundPosition: 'right',
        },
        '& a': {
            display: 'none',
            fontSize: '100%',
            color: '#ffffff !important',
            textAlign: 'center',
            margin: 'auto',
            position: 'absolute',
            top: 0,
            left: 0,
            bottom: 0,
            right: 0,
            height: '50px',
            cursor: 'pointer',
            textDecoration: 'none',
        },
    },

    imageWrapper: {
        position: 'relative',
        width: '100%',
        paddingBottom: '100%',
        backgroundSize: 'cover',
        backgroundPosition: 'center center',
        backgroundRepeat: 'no-repeat',
        transition: 'all .5s linear',

        cursor: 'pointer',
        boxShadow: '0 10px 6px -6px rgba(0, 0, 0, 0.3), 0 0 40px rgba(0, 0, 0, 0.1) inset',
    },

//
//     .imageGridItem a {
//         display: none;
//         font-size: 100%;
//         color: #ffffff !important;
//         text-align: center;
//         margin: auto;
//         position: absolute;
//         top: 0;
//         left: 0;
//         bottom: 0;
//         right: 0;
//         height: 50px;
//         cursor: pointer;
//         text-decoration: none;
//     }

}));

type ImageListProps = {
    activeCollection: Collection,
    columns?: number,
    onImageSelected?: (asset: Asset ) => void
}

export default function ImageList({columns = 5, activeCollection, onImageSelected}: ImageListProps) {
    const classes = useStyles();
    const assetRepository = new AssetRepository();

    const [assets, setAssets] = useState()

    useEffect( () => {
        assetRepository.assetsFor(activeCollection)
            .then(assetsRetrieved => {
                setAssets(assetsRetrieved)
            })
    }, [])

    if(assets === undefined) {
        return (
            <div className={classes.loader}>
                <CircularProgress />
            </div>
        )
    }

    return (
        <div className={classes.root}>
            <ImageGrid assets={assets} columns={columns} onClickHandler={onImageSelected}/>
        </div>
    )
}

type ImageGridProps = {
    assets: any,
    columns: number,
    onClickHandler?: (asset: Asset ) => void
}

function ImageGrid({assets, columns, onClickHandler}: ImageGridProps) {
    const classes = useStyles();

    const percentWidth = 100 / columns - 1;
    const style = { width: `${percentWidth}%` }

    return assets.map((asset: Asset, index: number) => (
        <div className={classes.imageGridItem}
            style={style}
            key={asset.id}>

            <ImageElement asset={asset} index={index} onClickHandler={onClickHandler}/>
        </div>
    ))
}

type ImageElementProps = {
    asset: Asset,
    index: number,
    onClickHandler?: (asset: Asset ) => void
}

function ImageElement({asset, index, onClickHandler}: ImageElementProps) {
    const classes = useStyles();
    const url = `collections/${asset.collectionId}/assets/${asset.id}/thumbnail`
    const style = {backgroundImage: `url(${url})`}
    return (
            <div className={classes.imageWrapper}
                onClick={() => onClickHandler && onClickHandler(asset)}
                style={style}>

                <a href="#">{asset.originalFilename}</a>
            </div>
    )
}