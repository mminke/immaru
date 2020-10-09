import React, { useState, useEffect } from 'react';
import { makeStyles } from '@material-ui/core/styles';
import GridList from '@material-ui/core/GridList';
import GridListTile from '@material-ui/core/GridListTile';
import GridListTileBar from '@material-ui/core/GridListTileBar';
import ListSubheader from '@material-ui/core/ListSubheader';
import IconButton from '@material-ui/core/IconButton';
import InfoIcon from '@material-ui/icons/Info';
import AssetRepository, {Asset} from '../repositories/AssetRepository'
import {Collection} from '../repositories/CollectionRepository'

const useStyles = makeStyles((theme) => ({
  gridList: {
    //width: 500,
    //height: 450,
  },
  icon: {
    color: 'rgba(255, 255, 255, 0.54)',
  },
}));

type ImageListProps = {
    activeCollection: Collection
    onImageSelected: (asset: Asset ) => void
}

export default function ImageList({activeCollection, onImageSelected}: ImageListProps) {
    const classes = useStyles();
    const assetRepository = new AssetRepository();

    const [assets, setAssets] = useState()

    useEffect( () => {
        assetRepository.assetsFor(activeCollection)
            .then(assetsRetrieved => {
                setAssets(assetsRetrieved)
            })
    }, [])

    if(assets === undefined) return null;

    return (
        <GridList cellHeight={180} className={classes.gridList} cols={4}>
            {assets.map( (asset:any) => (
            <GridListTile key={asset.id} onClick={() => onImageSelected(asset)}>
                <img src={'collections/' + activeCollection.id + '/assets/' + asset.id} alt={asset.originalFilename} />
                    <GridListTileBar
                        title={asset.originalFileName}
                        subtitle={<span>file: {asset.originalFilename}</span>}
                        actionIcon={
                            <IconButton aria-label={`info about ${asset.id}`} className={classes.icon}>
                                <InfoIcon />
                            </IconButton>
                        }
                    />
            </GridListTile>
            ))}
        </GridList>
    );
}