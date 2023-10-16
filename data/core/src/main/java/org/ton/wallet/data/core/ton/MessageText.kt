package org.ton.wallet.data.core.ton

import org.ton.api.pk.PrivateKey
import org.ton.api.pub.PublicKey
import org.ton.cell.CellBuilder
import org.ton.cell.CellSlice
import org.ton.tlb.*
import org.ton.tlb.providers.TlbCombinatorProvider
import org.ton.tlb.providers.TlbConstructorProvider

sealed interface MessageText {
    data class Raw(
        val text: String
    ) : MessageText {
        fun encrypt(publicKey: PublicKey): Encrypted {
            val encrypted = publicKey.encrypt(text.encodeToByteArray())
            return Encrypted(encrypted.asByteString())
        }

        companion object : TlbConstructorProvider<Raw> by TextTlbConstructor
    }

    data class Encrypted(
        val text: ByteString
    ) : MessageText {
        fun decrypt(privateKey: PrivateKey): Raw {
            val decrypted = privateKey.decrypt(text.toByteArray())
            return Raw(decrypted.decodeToString())
        }

        companion object : TlbConstructorProvider<Encrypted> by EncryptedTextTlbConstructor
    }

    companion object : TlbCombinatorProvider<MessageText> by MessageTextTlbCombinator
}

private object MessageTextTlbCombinator : TlbCombinator<MessageText>(
    MessageText::class,
    MessageText.Raw::class to TextTlbConstructor,
    MessageText.Encrypted::class to EncryptedTextTlbConstructor
)

private object TextTlbConstructor : TlbConstructor<MessageText.Raw>(
    "raw#00 text:BitString = MessageText"
) {
    override fun loadTlb(cellSlice: CellSlice): MessageText.Raw {
        val text = cellSlice.loadTlb(CellStringTlbConstructor)
        return MessageText.Raw(text.decodeToString())
    }

    override fun storeTlb(cellBuilder: CellBuilder, value: MessageText.Raw) {
        cellBuilder.storeTlb(CellStringTlbConstructor, value.text.encodeToByteArray().asByteString())
    }
}

private object EncryptedTextTlbConstructor : TlbConstructor<MessageText.Encrypted>(
    "encrypted#01 text:BitString = MessageText"
) {
    override fun loadTlb(cellSlice: CellSlice): MessageText.Encrypted {
        val text = cellSlice.loadTlb(CellStringTlbConstructor)
        return MessageText.Encrypted(text)
    }

    override fun storeTlb(cellBuilder: CellBuilder, value: MessageText.Encrypted) {
        cellBuilder.storeTlb(CellStringTlbConstructor, value.text)
    }
}
