package com.earthrevealed.immaru.exceptions

import com.earthrevealed.immaru.assets.AssetId
import com.earthrevealed.immaru.domain.CollectionId

class AssetNotFoundException(collectionId: CollectionId, assetId: AssetId) :
        Throwable("Collection: $collectionId, Asset: $assetId")
