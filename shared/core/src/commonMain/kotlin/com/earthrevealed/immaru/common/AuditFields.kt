package com.earthrevealed.immaru.common

import kotlinx.serialization.Serializable
import kotlin.time.Instant

@Serializable
class AuditFields(val createdAt: Instant = ClockProvider.clock.now().truncatedToMicroseconds()) {

    var lastModifiedAt: Instant = createdAt
        private set

    internal constructor(createdAt: Instant, lastModifiedAt: Instant) : this(createdAt.truncatedToMicroseconds()) {
        this.lastModifiedAt = lastModifiedAt.truncatedToMicroseconds()
    }

    fun registerModification() {
        lastModifiedAt = ClockProvider.clock.now().truncatedToMicroseconds()
    }

    override fun equals(other: Any?): Boolean {
        if (other is AuditFields) {
            return this.createdAt == other.createdAt && this.lastModifiedAt == other.lastModifiedAt
        }
        return false
    }

    override fun hashCode(): Int {
        var result = createdAt.hashCode()
        result = 31 * result + lastModifiedAt.hashCode()
        return result
    }

}
