package com.earthrevealed.medialibrary.api.v1

import com.earthrevealed.medialibrary.domain.AssetId
import com.earthrevealed.medialibrary.domain.CollectionId
import com.earthrevealed.medialibrary.domain.TagId
import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializerProvider
import org.springframework.boot.jackson.JsonComponent
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


@JsonComponent
class TagIdToStringConverter: JsonSerializer<TagId>() {
    override fun serialize(tagId: TagId?, jsonGenerator: JsonGenerator, serializerProvider: SerializerProvider?) {
        if(tagId == null) {
            jsonGenerator.writeNull()
        } else {
            jsonGenerator.writeString(tagId.value.toString())
        }
    }
}

@JsonComponent
class AssetIdToStringConverter: JsonSerializer<AssetId>() {
    override fun serialize(assetId: AssetId?, jsonGenerator: JsonGenerator, serializerProvider: SerializerProvider?) {
        if(assetId == null) {
            jsonGenerator.writeNull()
        } else {
            jsonGenerator.writeString(assetId.value.toString())
        }
    }
}

@JsonComponent
class CollectionIdToStringConverter: JsonSerializer<CollectionId>() {
    override fun serialize(collectionId: CollectionId?, jsonGenerator: JsonGenerator, serializerProvider: SerializerProvider?) {
        if(collectionId == null) {
            jsonGenerator.writeNull()
        } else {
            jsonGenerator.writeString(collectionId.value.toString())
        }
    }
}