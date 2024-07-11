package com.earthrevealed.immaru.collections

import com.benasher44.uuid.Uuid
import com.benasher44.uuid.uuid4
import com.benasher44.uuid.uuidFrom
import com.earthrevealed.immaru.common.ImmaruBuilder

data class CollectionId(
    val value: Uuid = uuid4()
) {
    companion object {
        fun fromString(value: String) = CollectionId(
            uuidFrom(value)
        )
    }
}

data class Collection(
    val id: CollectionId,
    val name: String,
    val createdAt: String
)

fun collection(initialization: CollectionBuilder.() -> Unit) =
    CollectionBuilder().apply(initialization).build()

@ImmaruBuilder
class CollectionBuilder {
    var id = CollectionId()
    var name = "Default"
    var creationDateTime: String = "creation Date"
//    var creationDateTime: OffsetDateTime = OffsetDateTime.now(ClockProvider.clock)

    fun build() = Collection(
        id = id,
        name = name,
        createdAt = creationDateTime
    )
}