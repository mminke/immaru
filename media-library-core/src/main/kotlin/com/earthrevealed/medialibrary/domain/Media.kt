package com.earthrevealed.medialibrary.domain

import com.earthrevealed.medialibrary.common.ClockProvider
import java.nio.file.Path
import java.time.OffsetDateTime
import java.util.*

data class Media(
        val originalFilename: Path,
        val id: UUID = UUID.randomUUID(),
        val creationDateTime: OffsetDateTime = OffsetDateTime.now(ClockProvider.clock)
)
