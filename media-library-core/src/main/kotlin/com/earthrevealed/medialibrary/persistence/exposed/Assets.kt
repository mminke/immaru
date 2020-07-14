package com.earthrevealed.medialibrary.persistence.exposed

import com.earthrevealed.medialibrary.domain.Asset
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.`java-time`.timestamp
import org.jetbrains.exposed.sql.statements.InsertStatement

internal object AssetTable : UUIDTable("assets") {
    val originalFilename = text("original_filename")
    val createdAt = timestamp( "created_at")
}

internal fun InsertStatement<Number>.from(asset: Asset) {
    this[AssetTable.id] = EntityID(asset.id.value, AssetTable)
    this[AssetTable.originalFilename] = asset.originalFilename
    this[AssetTable.createdAt] = asset.createdAt.toInstant()
}