package com.earthrevealed.immaru.common

import kotlinx.serialization.Serializable
import kotlin.time.Instant

@Serializable
class AuditFields(val createdAt: Instant = ClockProvider.clock.now()) {

    var lastModifiedAt: Instant = createdAt
        private set

    internal constructor(createdAt: Instant, lastModifiedAt: Instant) : this(createdAt) {
        this.lastModifiedAt = lastModifiedAt
    }

    fun registerModification() {
        lastModifiedAt = ClockProvider.clock.now()
    }
}
