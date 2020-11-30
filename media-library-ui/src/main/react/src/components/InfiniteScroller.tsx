import React, { useState, useEffect } from 'react';
import { makeStyles } from '@material-ui/core/styles';

import { FixedSizeGrid as Grid } from 'react-window';
import AutoSizer from "react-virtualized-auto-sizer";
import CircularProgress from '@material-ui/core/CircularProgress';

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
        width: '100%',
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
            display: 'block',
            color: '#ffffff !important',
            textAlign: 'center',
            margin: 'auto',
            position: 'absolute',
            bottom: 0,
            left: 0,
            width: '100%',
            cursor: 'pointer',
            textDecoration: 'none',
            paddingTop: '3px',
            paddingBottom: '3px',
            backgroundColor: 'rgba(255,255,255,0.2)',
        },
    },

    imageWrapper: {
        position: 'relative',
        width: '100%',
        paddingBottom: '100%',
        backgroundSize: 'cover',
//         backgroundSize: 'contain',
        backgroundPosition: 'center center',
        backgroundRepeat: 'no-repeat',
        transition: 'all .5s linear',

        cursor: 'pointer',
        boxShadow: '0 10px 6px -6px rgba(0, 0, 0, 0.3), 0 0 40px rgba(0, 0, 0, 0.1) inset',
    },


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
    <AutoSizer>
        {({ height, width }) => {

            const columnCount = 5
            const rowCount = Math.ceil(assets.length / columnCount)
            const imageSize = width/columnCount - 5

            return (
                    <Grid
                        height={height}
                        width={width}
                        columnCount={columnCount}
                        rowCount={rowCount}
                        columnWidth={imageSize}
                        rowHeight={imageSize}
                        itemData={assets}
                        overscanRowCount={2}
                    >
                        {Cell(onImageSelected)}
                    </Grid>
                )}


        }
    </AutoSizer>
      )
}


const Cell = (onImageSelected?: (asset: Asset ) => void) => ( {columnIndex, data, rowIndex, style}: any) => {

    const index = (rowIndex*5) + columnIndex
    if(index >= data.length) {
        return (
           <div style={style}>
           </div>
        )
    }

    const asset = data[index]
    return (
       <div style={style}>
           <ImageElement asset={asset} index={index} onClickHandler={onImageSelected}/>
       </div>
    )
};

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

        <div className={classes.imageGridItem}
            key={asset.id}>

            <div className={classes.imageWrapper}
                onClick={() => onClickHandler && onClickHandler(asset)}
                style={style}>

                <a href="#">{asset.originalFilename}</a>
            </div>
        </div>
    )
}