package com.earthrevealed.immaru.maintenance.api

import com.earthrevealed.immaru.collections.CollectionId
import io.ktor.resources.Resource

@Resource("maintenance")
class Maintenance {

    @Resource("orphaned/{collectionId}")
    class OrphanedAssets(
        @Suppress("unused") val parent: Maintenance = Maintenance(),
        val collectionId: CollectionId
    )

}
