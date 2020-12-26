import React, { useState, useEffect, MouseEvent  } from 'react';
import { makeStyles } from '@material-ui/core/styles';
import clsx from 'clsx';
import { useHistory } from "react-router-dom"

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
    imageSelected: {
        backgroundColor: 'lightgrey',
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

export default function ImageList({
                                    columns = 5,
                                    activeCollection,
                                    onImageSelected: handleImageSelected
                                  }: ImageListProps) {
    const classes = useStyles()
    const history = useHistory()
    const assetRepository = new AssetRepository()

    const [assets, setAssets] = useState()
    const [selectedAssets, setSelectedAssets] = useState<Array<Asset>>([])

    useEffect( () => {
        assetRepository.assetsFor(activeCollection)
            .then(assetsRetrieved => {
                setAssets(assetsRetrieved)
            })
    }, [activeCollection])

    const isSelected = (asset: Asset): boolean => {
        return selectedAssets.includes(asset)
    }

    const handleImageClick = (event: MouseEvent, asset: Asset) => {
        console.log("aMouseEvent: ", event)
        console.log("Shift: ", event.shiftKey)
        console.log("Ctrl: ", event.ctrlKey)

        setSelectedAssets([asset])
        if(handleImageSelected != undefined) {
            handleImageSelected(asset)
        }

        if(event.ctrlKey) {
            history.push("/asset/" + asset.id)
        }
    }

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
                        {Cell(handleImageClick, isSelected)}
                    </Grid>
                )}
        }
    </AutoSizer>
      )
}


const Cell = (
        handleClick?: (event: MouseEvent, asset: Asset ) => void,
        isSelected?: (asset: Asset) => boolean
    ) => ( {columnIndex, data, rowIndex, style}: any) => {
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
                <ImageElement
                    asset={asset}
                    index={index}
                    onClick={handleClick}
                    isSelected={isSelected}
                />
            </div>
        )
    };

type ImageElementProps = {
    asset: Asset,
    index: number,
    onClick?: (event: MouseEvent, asset: Asset ) => void
    isSelected?: (asset: Asset ) => boolean
}

function ImageElement({asset, index, onClick: handleClick, isSelected: isAssetSelected}: ImageElementProps) {
    const classes = useStyles();
    const url = `collections/${asset.collectionId}/assets/${asset.id}/thumbnail`
    const style = {backgroundImage: `url(${url})`}

    let isSelected = false
    if(isAssetSelected !== undefined) {
        isSelected = isAssetSelected(asset)
    }

    const handleOnClick = (event: MouseEvent, asset: Asset) => {
        event.persist()
        handleClick && handleClick(event, asset)
    }

    return (

        <div className={clsx(classes.imageGridItem, {
                            [classes.imageSelected]: isSelected,
                        })}
            key={asset.id}>

            <div className={classes.imageWrapper}
                onClick={(event: MouseEvent) => handleOnClick(event, asset)}
                style={style}>

                <a href="#">{asset.originalFilename}</a>
            </div>
        </div>
    )
}