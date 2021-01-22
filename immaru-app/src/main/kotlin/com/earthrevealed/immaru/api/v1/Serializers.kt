package com.earthrevealed.immaru.api.v1

import com.earthrevealed.immaru.domain.AssetId
import com.earthrevealed.immaru.domain.CollectionId
import com.earthrevealed.immaru.domain.MediaType
import com.earthrevealed.immaru.domain.TagId
import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializerProvider
import org.springframework.boot.jackson.JsonComponent

@JsonComponent
class TagIdJsonSerializer: JsonSerializer<TagId>() {
    override fun serialize(tagId: TagId?, jsonGenerator: JsonGenerator, serializerProvider: SerializerProvider?) {
        if(tagId == null) {
            jsonGenerator.writeNull()
        } else {
            jsonGenerator.writeString(tagId.value.toString())
        }
    }
}

@JsonComponent
class TagIdJsonDeserializer: JsonDeserializer<TagId>() {
    override fun deserialize(jsonParser: JsonParser, context: DeserializationContext): TagId {
        val value = jsonParser.valueAsString
        return TagId(value)
    }
}

@JsonComponent
class AssetIdJsonSerializer: JsonSerializer<AssetId>() {
    override fun serialize(assetId: AssetId?, jsonGenerator: JsonGenerator, serializerProvider: SerializerProvider?) {
        if(assetId == null) {
            jsonGenerator.writeNull()
        } else {
            jsonGenerator.writeString(assetId.value.toString())
        }
    }
}

@JsonComponent
class CollectionIdJsonSerializer: JsonSerializer<CollectionId>() {
    override fun serialize(collectionId: CollectionId?, jsonGenerator: JsonGenerator, serializerProvider: SerializerProvider?) {
        if(collectionId == null) {
            jsonGenerator.writeNull()
        } else {
            jsonGenerator.writeString(collectionId.value.toString())
        }
    }
}

@JsonComponent
class MediaTypeJsonSerializer: JsonSerializer<MediaType>() {
    override fun serialize(mediaType: MediaType?, jsonGenerator: JsonGenerator, serializerProvider: SerializerProvider?) {
        if(mediaType == null) {
            jsonGenerator.writeNull()
        } else {
            jsonGenerator.writeString(mediaType.value.toString())
        }
    }
}