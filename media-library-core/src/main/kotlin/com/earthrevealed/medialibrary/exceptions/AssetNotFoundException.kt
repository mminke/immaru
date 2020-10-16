package com.earthrevealed.medialibrary.exceptions

import com.earthrevealed.medialibrary.domain.AssetId
import com.earthrevealed.medialibrary.domain.CollectionId

class AssetNotFoundException(collectionId: CollectionId, assetId: AssetId) :
        Throwable("Collection: $collectionId, Asset: $assetId")
