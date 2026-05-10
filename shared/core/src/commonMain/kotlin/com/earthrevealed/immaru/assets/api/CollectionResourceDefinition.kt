package com.earthrevealed.immaru.assets.api

import com.earthrevealed.immaru.assets.AssetId
import com.earthrevealed.immaru.collections.CollectionId
import io.ktor.resources.Resource

@Resource("collections")
class Collections {

    @Resource("{id1}")
    class ById(val parent: Collections = Collections(), val id1: CollectionId) {

        @Resource("assets")
        class Assets(
            val collection: Collections.ById,
            val limit: Int? = null,
            val direction: String = "FORWARD",
            val cursorCreatedAt: String? = null,
            val cursorId: String? = null,
        ) {

            @Resource("{id2}")
            class ById(val parent: Assets, val id2: AssetId) {

                @Resource("content")
                class Content(val asset: Assets.ById)
            }
        }

        @Resource("available-date-selectors")
        class Selectors(val collection: ById)
    }
}
