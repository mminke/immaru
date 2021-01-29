package com.earthrevealed.immaru.api.v1

import com.earthrevealed.immaru.domain.AssetId
import com.earthrevealed.immaru.domain.CollectionId
import com.earthrevealed.immaru.domain.CreatedAt
import com.earthrevealed.immaru.domain.Height
import com.earthrevealed.immaru.domain.LastModifiedAt
import com.earthrevealed.immaru.domain.OriginalDateOfCreation
import com.earthrevealed.immaru.domain.TagId
import com.earthrevealed.immaru.domain.Width
import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializerProvider
import org.springframework.boot.jackson.JsonComponent
import javax.ws.rs.core.MediaType

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
            jsonGenerator.writeString(mediaType.toString())
        }
    }
}

@JsonComponent
class OriginalDateOfCreationJsonSerializer: JsonSerializer<OriginalDateOfCreation>() {
    override fun serialize(originalDateOfCreation: OriginalDateOfCreation, jsonGenerator: JsonGenerator, serializerProvider: SerializerProvider?) {
        if(originalDateOfCreation == null) {
            jsonGenerator.writeNull()
        } else {
            jsonGenerator.writeString(originalDateOfCreation.value.toString())
        }
    }
}

@JsonComponent
class CreatedAtJsonSerializer: JsonSerializer<CreatedAt>() {
    override fun serialize(createdAt: CreatedAt, jsonGenerator: JsonGenerator, serializerProvider: SerializerProvider?) {
        if(createdAt == null) {
            jsonGenerator.writeNull()
        } else {
            jsonGenerator.writeString(createdAt.value.toString())
        }
    }
}

@JsonComponent
class LastModifiedAtJsonSerializer: JsonSerializer<LastModifiedAt>() {
    override fun serialize(lastModifiedAt: LastModifiedAt, jsonGenerator: JsonGenerator, serializerProvider: SerializerProvider?) {
        if(lastModifiedAt == null) {
            jsonGenerator.writeNull()
        } else {
            jsonGenerator.writeString(lastModifiedAt.value.toString())
        }
    }
}

@JsonComponent
class ImageWidthJsonSerializer: JsonSerializer<Width>() {
    override fun serialize(width: Width, jsonGenerator: JsonGenerator, serializerProvider: SerializerProvider?) {
        if(width == null) {
            jsonGenerator.writeNull()
        } else {
            jsonGenerator.writeString(width.value.value.toString())
        }
    }
}

@JsonComponent
class ImageHeightJsonSerializer: JsonSerializer<Height>() {
    override fun serialize(height: Height, jsonGenerator: JsonGenerator, serializerProvider: SerializerProvider?) {
        if(height == null) {
            jsonGenerator.writeNull()
        } else {
            jsonGenerator.writeString(height.value.value.toString())
        }
    }
}