package com.earthrevealed.medialibrary.api.v1

import com.earthrevealed.medialibrary.domain.AssetId
import com.earthrevealed.medialibrary.domain.CollectionId
import com.earthrevealed.medialibrary.domain.TagId
import org.springframework.core.convert.converter.Converter
import org.springframework.stereotype.Component
import java.util.*

@Component
class StringToAssetIdConverter: Converter<String, AssetId> {
    override fun convert(uuid: String) = AssetId(UUID.fromString(uuid))
}

@Component
class StringToCollectionIdConverter: Converter<String, CollectionId> {
    override fun convert(uuid: String) = CollectionId(UUID.fromString(uuid))
}

@Component
class StringToTagIdConverter: Converter<String, TagId> {
    override fun convert(uuid: String) = TagId(UUID.fromString(uuid))
}