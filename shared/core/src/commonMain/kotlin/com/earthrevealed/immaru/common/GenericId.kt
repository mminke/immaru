package com.earthrevealed.immaru.common

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlin.uuid.Uuid


abstract class GenericId(val value: Uuid) {
    override fun toString(): String {
        return value.toString()
    }

    override fun equals(other: Any?): Boolean {
        return if(other is GenericId) {
            this.value == other.value
        } else {
            false
        }
    }

    override fun hashCode(): Int {
        return value.hashCode()
    }
}

abstract class GenericIdSerializer<T : GenericId>(val creator: (String) -> T) : KSerializer<T> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("GenericId", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: T) {
        encoder.encodeString(value.value.toString())
    }

    override fun deserialize(decoder: Decoder): T {
        val value = decoder.decodeString()
        return creator(value)
    }
}