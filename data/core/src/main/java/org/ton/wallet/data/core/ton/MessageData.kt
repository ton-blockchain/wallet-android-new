package org.ton.wallet.data.core.ton

import org.ton.api.pub.PublicKey
import org.ton.block.StateInit
import org.ton.cell.Cell
import org.ton.tlb.CellRef

sealed interface MessageData {

    val destination: String
    val amount: Long
    val stateInit: StateInit?
    val body: Cell?
    val sendMode: Int

    data class Raw(
        override val destination: String,
        override val amount: Long,
        override val body: Cell?,
        override val stateInit: StateInit?,
        // TODO: make sendMode required and rewrite with flags, see https://docs.ton.org/develop/smart-contracts/messages#message-modes
        override val sendMode: Int = -1
    ) : MessageData

    data class Text(
        override val destination: String,
        override val amount: Long,
        val text: CellRef<MessageText>,
        // TODO: make sendMode required and rewrite with flags
        override val sendMode: Int = -1
    ) : MessageData {

        constructor(
            destination: String,
            amount: Long,
            text: MessageText,
            // TODO: make sendMode required and rewrite with flags
            sendMode: Int = -1
        ) : this(destination, amount, CellRef(text, MessageText), sendMode)

        override val body: Cell
            get() = text.toCell(MessageText)

        override val stateInit: StateInit?
            get() = null
    }

    companion object {

        fun raw(
            destination: String,
            amount: Long,
            body: Cell? = null,
            stateInit: StateInit? = null,
            // TODO: make sendMode required and rewrite with flags
            sendMode: Int = -1
        ): Raw {
            return Raw(destination, amount, body, stateInit, sendMode)
        }

        fun text(destination: String, amount: Long, text: String, sendMode: Int = -1): Text {
            return Text(destination, amount, MessageText.Raw(text), sendMode)
        }

        fun encryptedText(
            destination: String,
            amount: Long,
            publicKey: PublicKey,
            text: String,
            // TODO: make sendMode required and rewrite with flags
            sendMode: Int = -1
        ): Text {
            return Text(destination, amount, MessageText.Raw(text).encrypt(publicKey), sendMode)
        }
    }
}