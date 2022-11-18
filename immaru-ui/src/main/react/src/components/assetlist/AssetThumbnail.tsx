import React, { useState, useEffect, MouseEvent  } from 'react';
import { makeStyles } from '@material-ui/core/styles';
import clsx from 'clsx';
import { useNavigate } from "react-router-dom"
import {useHotkeys} from "react-hotkeys-hook"

import { FixedSizeGrid as Grid } from 'react-window';
import AutoSizer from "react-virtualized-auto-sizer";
import CircularProgress from '@material-ui/core/CircularProgress';

import {Asset} from '../../repositories/AssetRepository'
import {Collection} from '../../repositories/CollectionRepository'

const useStyles = makeStyles((theme) => ({
    assetThumbnail: {
        position: 'relative',
        width: '100%',
        paddingBottom: '100%',
        backgroundSize: 'cover',
//         backgroundSize: 'contain',
        backgroundPosition: 'center center',
        backgroundRepeat: 'no-repeat',

        transition: 'all .5s linear',
        transitionDelay: '.1s',
        '&:hover': {
            backgroundPosition: 'right',
        },
        cursor: 'pointer',
        boxShadow: '0 10px 6px -6px rgba(0, 0, 0, 0.3), 0 0 40px rgba(0, 0, 0, 0.1) inset',
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
}));

type Props = {
    asset: Asset,
}

export const AssetThumbnail = ({asset,}: Props) => {

    const classes = useStyles();

    const url = `collections/${asset.collectionId}/assets/${asset.id}/thumbnail`
    const style = {backgroundImage: `url(${url})`}

    return (
        <div className={classes.assetThumbnail}
            style={style}>

            <a href="#">{asset.originalFilename}</a>
        </div>
    )
}