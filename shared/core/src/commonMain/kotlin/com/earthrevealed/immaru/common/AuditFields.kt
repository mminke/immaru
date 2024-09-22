package com.earthrevealed.immaru.common

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Serializable
class AuditFields(val createdOn: Instant = ClockProvider.clock.now()) {

    var lastModifiedOn: Instant = createdOn
        private set

    internal constructor(createdOn: Instant, lastModifiedOn: Instant) : this(createdOn) {
        this.lastModifiedOn = lastModifiedOn
    }

    fun registerModification() {
        lastModifiedOn = ClockProvider.clock.now()
    }
}
