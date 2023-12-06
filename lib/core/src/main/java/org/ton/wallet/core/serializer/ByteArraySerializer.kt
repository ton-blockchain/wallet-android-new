package org.ton.wallet.core.serializer

import kotlinx.serialization.*
import java.io.*

object ByteArraySerializer {

    internal val serializer = serializer<ByteArray>()

    @Throws(IllegalArgumentException::class)
    inline fun <reified T> deserialize(bytes: ByteArray): T {
        return deserialize(bytes, serializer<T>())
    }

    @Throws(IllegalArgumentException::class)
    fun <T> deserialize(bytes: ByteArray, deserializer: DeserializationStrategy<T>): T {
        val dataDecoder = ByteArraySerializerDecoder(DataInputStream(ByteArrayInputStream(bytes)))
        return dataDecoder.decodeSerializableValue(deserializer)
    }

    inline fun <reified T> serialize(value: T): ByteArray {
        return serialize(value, serializer<T>())
    }

    fun <T> serialize(value: T, serializer: SerializationStrategy<T>): ByteArray {
        val stream = ByteArrayOutputStream()
        val encoder = ByteArraySerializerEncoder(DataOutputStream(stream))
        encoder.encodeSerializableValue(serializer, value)
        return stream.toByteArray()
    }
}