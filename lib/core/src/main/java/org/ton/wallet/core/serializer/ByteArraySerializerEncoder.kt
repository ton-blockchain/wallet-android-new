package org.ton.wallet.core.serializer

import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.AbstractEncoder
import kotlinx.serialization.encoding.CompositeEncoder
import kotlinx.serialization.modules.EmptySerializersModule
import kotlinx.serialization.modules.SerializersModule
import java.io.DataOutput

internal class ByteArraySerializerEncoder(
    private val output: DataOutput
) : AbstractEncoder() {

    override val serializersModule: SerializersModule = EmptySerializersModule()

    override fun encodeBoolean(value: Boolean) {
        output.writeBoolean(value)
    }

    override fun encodeByte(value: Byte) {
        output.writeByte(value.toInt())
    }

    override fun encodeChar(value: Char) {
        output.writeChar(value.code)
    }

    override fun encodeDouble(value: Double) {
        output.writeDouble(value)
    }

    override fun encodeEnum(enumDescriptor: SerialDescriptor, index: Int) {
        output.writeInt(index)
    }

    override fun encodeFloat(value: Float) {
        output.writeFloat(value)
    }

    override fun encodeInt(value: Int) {
        output.writeInt(value)
    }

    override fun encodeLong(value: Long) {
        output.writeLong(value)
    }

    override fun encodeShort(value: Short) {
        output.writeShort(value.toInt())
    }

    override fun encodeString(value: String) {
        output.writeUTF(value)
    }

    override fun encodeNull() {
        encodeBoolean(false)
    }

    override fun encodeNotNullMark() {
        encodeBoolean(true)
    }

    override fun beginCollection(descriptor: SerialDescriptor, collectionSize: Int): CompositeEncoder {
        encodeInt(collectionSize)
        return this
    }

    override fun <T> encodeSerializableValue(serializer: SerializationStrategy<T>, value: T) {
        if (serializer.descriptor == ByteArraySerializer.serializer.descriptor) {
            encodeByteArray(value as ByteArray)
        } else {
            super.encodeSerializableValue(serializer, value)
        }
    }

    private fun encodeByteArray(bytes: ByteArray) {
        encodeCompactSize(bytes.size)
        output.write(bytes)
    }

    private fun encodeCompactSize(value: Int) {
        if (value < 0xff) {
            output.writeByte(value)
        } else {
            output.writeByte(0xff)
            output.writeInt(value)
        }
    }
}