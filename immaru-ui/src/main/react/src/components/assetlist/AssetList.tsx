import React, { useState, useEffect, MouseEvent  } from 'react';
import { makeStyles } from '@material-ui/core/styles';
import clsx from 'clsx';
import { useNavigate } from "react-router-dom"
import {useHotkeys} from "react-hotkeys-hook"

import { FixedSizeGrid as Grid } from 'react-window';
import AutoSizer from "react-virtualized-auto-sizer";
import CircularProgress from '@material-ui/core/CircularProgress';

import SelectTagsDialog from '../SelectTagsDialog'
import AddToGroupDialog from '../AddToGroupDialog'
import {Tag} from '../../repositories/TagRepository'
import {assetRepository, Asset} from '../../repositories/AssetRepository'
import {Collection} from '../../repositories/CollectionRepository'

import {AssetThumbnail} from './AssetThumbnail'

import {hotkeysEnabledFilter} from '../../HotkeyState'

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
    },
    imageSelected: {
        backgroundColor: theme.palette.primary.light,
    },
}));

type AssetListProps = {
    activeCollection: Collection,
    assets: Asset[],
    columns?: number,
    onImageSelected?: (asset: Asset ) => void
}

export default function AssetList({
    activeCollection,
    assets,
    columns = 7,
    onImageSelected: handleImageSelected
}: AssetListProps) {

    const classes = useStyles()
    const navigate = useNavigate()

    const [selectedAssets, setSelectedAssets] = useState<Array<Asset>>([])

    const [selectTagsDialogIsOpen, setSelectTagsDialogIsOpen] = useState(false)
    const [addToGroupDialogIsOpen, setAddToGroupDialogIsOpen] = useState(false)

    const isSelected = (asset: Asset): boolean => {
        return selectedAssets.includes(asset)
    }

    const handleImageClick = (event: MouseEvent, asset: Asset) => {
        if(!event.ctrlKey && !event.shiftKey) {
            setSelectedAssets([asset])
        }

        if(event.ctrlKey && !event.shiftKey) {
            if(selectedAssets.includes(asset)) {
                const newSelectedAssets = selectedAssets.filter(item => item !== asset)
                setSelectedAssets(newSelectedAssets)
            } else {
                const newSelectedAssets = selectedAssets.concat(asset)
                setSelectedAssets(newSelectedAssets)
            }
        }
        if(event.shiftKey && !event.ctrlKey) {
            if(selectedAssets.length > 0) {
                const firstSelectedAssetIndex = assets.indexOf(selectedAssets[0])
                const currentAssetIindex = assets.indexOf(asset)
                const startIndex = Math.min(firstSelectedAssetIndex, currentAssetIindex)
                const endIndex = Math.max(firstSelectedAssetIndex, currentAssetIindex)

                let newSelectedAssets = [] as Asset[]
                for(let i=startIndex; i <= endIndex; i++) {
                    newSelectedAssets.push(assets[i])
                }
                setSelectedAssets(newSelectedAssets)
            } else {
                setSelectedAssets([asset])
            }
        }

        if(handleImageSelected !== undefined) {
            handleImageSelected(asset)
        }
    }

    const handleHotkey_v = () => {
        if(selectedAssets.length > 0) {
            navigate("/asset/" + selectedAssets[0].id)
        }
    }

    const handleHotkey_t = () => {
        if(selectedAssets.length > 0) {
            setSelectTagsDialogIsOpen(true)
        }
    }

    const handleHotkey_g = () => {
        if(selectedAssets.length > 1) {
            setAddToGroupDialogIsOpen(true)
        }
    }

    const handleSelectTagsDialogClose = () => {
        setSelectTagsDialogIsOpen(false)
    }
    const handleAddToGroupDialogClose = () => {
        setAddToGroupDialogIsOpen(false)
    }

    const handleTagsSelected = (selectedTags: Array<Tag>) => {
        for(var asset of selectedAssets) {
            selectedTags.forEach( (tag) => {
                asset.tagIds.push(tag.id)
            })
            assetRepository.updateTagsFor(asset)
        }
        setSelectTagsDialogIsOpen(false)
    }

    useHotkeys('v', hotkeysEnabledFilter(handleHotkey_v), [selectedAssets]);
    useHotkeys('t', hotkeysEnabledFilter(handleHotkey_t), [selectedAssets]);
    useHotkeys('g', hotkeysEnabledFilter(handleHotkey_g), [selectedAssets]);

    if(assets === undefined) {
        return (
            <div className={classes.loader}>
                <CircularProgress />
            </div>
        )
    }

    return (
        <div style={{flexGrow: 1}}>
            <AutoSizer>
                {({ height, width }) => {

                    const columnCount = columns
                    const rowCount = Math.ceil(assets.length / columnCount)
                    const imageSize = width/columnCount - columnCount

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
                                useIsScrolling
                            >
                                {Cell(isSelected, columnCount, handleImageClick)}
                            </Grid>
                        )}
                }
            </AutoSizer>
            <SelectTagsDialog activeCollection={activeCollection} open={selectTagsDialogIsOpen} onClose={handleSelectTagsDialogClose} onSelect={handleTagsSelected}/>
            <AddToGroupDialog activeCollection={activeCollection} assets={selectedAssets} open={addToGroupDialogIsOpen} onClose={handleAddToGroupDialogClose}/>
        </div>
    )
}

const Cell = (
        isSelected: (asset: Asset) => boolean,
        columnCount: number,
        handleClick?: (event: MouseEvent, asset: Asset ) => void,
    ) => ( {columnIndex, data, rowIndex, style, isScrolling}: any) => {
        const index = (rowIndex*columnCount) + columnIndex
        if(index >= data.length) {
            return (
               <div style={style}>
               </div>
            )
        }

        const asset = data[index]
        return (
            <div style={style}>
            {
                isScrolling ? "Loading..." :
                    <ImageElement
                        asset={asset}
                        index={index}
                        onClick={handleClick}
                        isSelected={isSelected}
                    />
            }
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

    let isSelected = false
    if(isAssetSelected !== undefined) {
        isSelected = isAssetSelected(asset)
    }

    return (

        <div className={clsx(classes.imageGridItem, {
                            [classes.imageSelected]: isSelected,
                        })}
                        onClick={(event: MouseEvent) => handleClick && handleClick(event, asset)}
            key={asset.id}>
            <AssetThumbnail asset={asset} />
        </div>
    )
}
