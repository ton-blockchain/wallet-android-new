package org.ton.wallet.data.core.ton

import org.ton.api.pub.PublicKey
import org.ton.block.StateInit
import org.ton.cell.Cell
import org.ton.tlb.CellRef

sealed interface MessageData {

    val body: Cell?
    val stateInit: StateInit?

    data class Raw(
        override val body: Cell?,
        override val stateInit: StateInit?
    ) : MessageData

    data class Text(
        val text: CellRef<MessageText>
    ) : MessageData {

        constructor(text: MessageText) : this(CellRef(text, MessageText))

        override val body: Cell
            get() = text.toCell(MessageText)

        override val stateInit: StateInit?
            get() = null
    }

    companion object {

        fun raw(body: Cell? = null, stateInit: StateInit? = null): Raw {
            return Raw(body, stateInit)
        }

        fun text(text: String): Text {
            return Text(MessageText.Raw(text))
        }

        fun encryptedText(publicKey: PublicKey, text: String): Text {
            return Text(MessageText.Raw(text).encrypt(publicKey))
        }
    }
}