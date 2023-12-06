package org.ton.wallet.data.core.model

import org.ton.api.pk.PrivateKeyEd25519
import org.ton.api.pub.PublicKey
import org.ton.block.StateInit
import org.ton.boc.BagOfCells
import org.ton.cell.Cell
import org.ton.tlb.CellRef
import org.ton.wallet.data.core.ton.MessageText

sealed interface MessageData {

    val destination: String
    val amount: Long
    val stateInit: StateInit?
    val body: Cell?
    val sendMode: Int

    class Raw internal constructor(
        override val destination: String,
        override val amount: Long,
        override val body: Cell?,
        override val stateInit: StateInit?,
        override val sendMode: Int
    ) : MessageData

    class Text internal constructor(
        override val destination: String,
        override val amount: Long,
        val textCell: CellRef<MessageText>?,
        override val sendMode: Int,
    ) : MessageData {

        override val body: Cell? = textCell?.toCell(MessageText)
        override val stateInit: StateInit? = null
    }

    companion object {

        // https://docs.ton.org/develop/smart-contracts/messages#message-modes
        private const val ModeOrdinary = 0
        private const val ModeAllValue = 64
        private const val ModeAllBalance = 128

        private const val FlagSeparateFee = 1
        private const val FlagIgnoreErrors = 1 shl 1
        private const val FlagDestroyIfZero = 1 shl 5

        const val DefaultSendMode = -1
        const val OrdinarySendMode = ModeOrdinary or FlagSeparateFee or FlagIgnoreErrors
        const val AllBalanceSendMode = ModeAllBalance or FlagIgnoreErrors

        fun buildRaw(
            destination: String,
            amount: Long,
            body: Cell? = null,
            stateInit: StateInit? = null,
            sendMode: Int = DefaultSendMode
        ): Raw {
            return Raw(destination, amount, body, stateInit, sendMode)
        }

        fun buildText(
            destination: String,
            amount: Long,
            text: String?,
            sendMode: Int = DefaultSendMode
        ): Text {
            val textCellRef =
                if (text.isNullOrEmpty()) null
                else CellRef(MessageText.Raw(text), MessageText)
            return Text(destination, amount, textCellRef, sendMode)
        }

        fun buildEncryptedText(
            destination: String,
            amount: Long,
            publicKey: PublicKey,
            text: String,
            sendMode: Int = DefaultSendMode
        ): Text {
            return Text(destination, amount, CellRef(MessageText.Raw(text).encrypt(publicKey), MessageText), sendMode)
        }
    }
}

fun MessageData.getText(seed: ByteArray?): String? {
    return when (this) {
        is MessageData.Text -> {
            when (val messageText = this.textCell?.value) {
                is MessageText.Raw -> {
                    messageText.text
                }
                is MessageText.Encrypted -> {
                    if (seed == null) null
                    else messageText.decrypt(PrivateKeyEd25519(seed)).text
                }
                else -> null
            }
        }
        is MessageData.Raw -> {
            try {
                this.body?.let { body ->
                    BagOfCells(body).roots.firstOrNull()?.let { cell ->
                        (MessageText.loadTlb(cell) as? MessageText.Raw)?.text
                    }
                }
            } catch (e: Exception) {
                null
            }
        }
    }
}