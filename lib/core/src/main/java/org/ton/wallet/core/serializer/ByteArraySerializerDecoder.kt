package org.ton.wallet.core.serializer

import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.AbstractDecoder
import kotlinx.serialization.encoding.CompositeDecoder
import kotlinx.serialization.modules.EmptySerializersModule
import kotlinx.serialization.modules.SerializersModule
import java.io.DataInput

internal class ByteArraySerializerDecoder(
    private val input: DataInput,
    var elementsCount: Int = 0
) : AbstractDecoder() {

    private var elementIndex = 0

    override val serializersModule: SerializersModule = EmptySerializersModule()

    override fun decodeBoolean(): Boolean {
        return input.readByte().toInt() != 0
    }

    override fun decodeByte(): Byte {
        return input.readByte()
    }

    override fun decodeChar(): Char {
        return input.readChar()
    }

    override fun decodeDouble(): Double {
        return input.readDouble()
    }

    override fun decodeEnum(enumDescriptor: SerialDescriptor): Int {
        return input.readInt()
    }

    override fun decodeFloat(): Float {
        return input.readFloat()
    }

    override fun decodeInt(): Int {
        return input.readInt()
    }

    override fun decodeLong(): Long {
        return input.readLong()
    }

    override fun decodeShort(): Short {
        return input.readShort()
    }

    override fun decodeString(): String {
        return input.readUTF()
    }

    override fun decodeElementIndex(descriptor: SerialDescriptor): Int {
        if (elementIndex == elementsCount) {
            return CompositeDecoder.DECODE_DONE
        }
        return elementIndex++
    }

    override fun beginStructure(descriptor: SerialDescriptor): CompositeDecoder {
        return ByteArraySerializerDecoder(input, descriptor.elementsCount)
    }

    override fun decodeSequentially(): Boolean {
        return true
    }

    override fun decodeCollectionSize(descriptor: SerialDescriptor): Int {
        return decodeInt().also { elementsCount = it }
    }

    override fun decodeNotNullMark(): Boolean {
        return decodeBoolean()
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T> decodeSerializableValue(deserializer: DeserializationStrategy<T>, previousValue: T?): T {
        return if (deserializer.descriptor == ByteArraySerializer.serializer.descriptor) {
            decodeByteArray() as T
        } else {
            super.decodeSerializableValue(deserializer, previousValue)
        }
    }

    private fun decodeByteArray(): ByteArray {
        val bytes = ByteArray(decodeCompactSize())
        input.readFully(bytes)
        return bytes
    }

    private fun decodeCompactSize(): Int {
        val byte = input.readByte().toInt() and 0xff
        if (byte < 0xff) {
            return byte
        }
        return input.readInt()
    }
}