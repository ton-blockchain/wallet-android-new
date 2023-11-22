package org.ton.wallet.data.core.ton

import kotlinx.datetime.Clock
import org.ton.api.pk.PrivateKeyEd25519
import org.ton.bitstring.BitString
import org.ton.block.*
import org.ton.boc.BagOfCells
import org.ton.cell.Cell
import org.ton.cell.CellBuilder
import org.ton.contract.wallet.WalletTransfer
import org.ton.tlb.*
import org.ton.tlb.constructor.AnyTlbConstructor
import org.ton.wallet.data.core.model.TonAccount
import kotlin.time.Duration.Companion.seconds

object TonWalletHelper {

    fun getTransferMessage(
        address: AddrStd,
        stateInit: StateInit?,
        transferCell: Cell,
    ): Message<Cell> {
        val info = ExtInMsgInfo(
            src = AddrNone,
            dest = address,
            importFee = Coins()
        )
        val maybeStateInit = Maybe.of(stateInit?.let { Either.of<StateInit, CellRef<StateInit>>(null, CellRef(it)) })
        val body = Either.of<Cell, CellRef<Cell>>(null, CellRef(transferCell))
        return Message(
            info = info,
            init = maybeStateInit,
            body = body
        )
    }

    fun getTransferCell(
        fromAccount: TonAccount,
        toWorkChainId: Int,
        toRawAddress: ByteArray,
        amount: Long,
        msgData: MessageData? = null,
        seed: ByteArray? = null
    ): Cell {
        var transferBody: Cell? = null
        if (msgData is MessageData.Raw) {
            transferBody = msgData.body
        } else if (msgData is MessageData.Text) {
            transferBody = CellBuilder.createCell {
                storeInt(0, 32)
                storeTlb(MessageText.Companion, msgData.text.value)
            }
        }

        val transfer = WalletTransfer {
            destination = AddrStd(toWorkChainId, toRawAddress)
            coins = Coins.ofNano(amount)
            body = transferBody
            stateInit = msgData?.stateInit?.value
        }

        val unsignedBody = CellBuilder.createCell {
            storeUInt(fromAccount.subWalletId, 32)
            storeUInt((Clock.System.now() + 60.seconds).epochSeconds.toInt(), 32)
            storeUInt(fromAccount.seqNo, 32)
            if (fromAccount.version == 4) {
                storeUInt(0, 8) // op
            }
            var sendMode = 3
            if (transfer.sendMode > -1) {
                sendMode = transfer.sendMode
            }
            val intMsg = CellRef(createIntMsg(transfer))
            storeUInt(sendMode, 8)
            storeRef(MessageRelaxed.tlbCodec(AnyTlbConstructor), intMsg)
        }
        val privateKey = seed?.let { PrivateKeyEd25519(it) }
        val signature = privateKey?.let { BitString(it.sign(unsignedBody.hash())) }

        return CellBuilder.createCell {
            signature?.let { storeBits(it) }
            storeBits(unsignedBody.bits)
            storeRefs(unsignedBody.refs)
        }
    }

    fun getMessageText(msgData: MessageData, seed: ByteArray?): String? {
        return when (msgData) {
            is MessageData.Text -> {
                when (val messageText = msgData.text.value) {
                    is MessageText.Raw -> {
                        messageText.text
                    }
                    is MessageText.Encrypted -> {
                        if (seed == null) null
                        else messageText.decrypt(PrivateKeyEd25519(seed)).text
                    }
                }
            }
            is MessageData.Raw -> {
                try {
                    BagOfCells(msgData.body).roots.firstOrNull()?.let { cell ->
                        (MessageText.loadTlb(cell) as? MessageText.Raw)?.text
                    }
                } catch (e: Exception) {
                    null
                }
            }
        }
    }

    private fun createIntMsg(transfer: WalletTransfer): MessageRelaxed<Cell> {
        val info = CommonMsgInfoRelaxed.IntMsgInfoRelaxed(
            ihrDisabled = true,
            bounce = transfer.bounceable,
            bounced = false,
            src = AddrNone,
            dest = transfer.destination,
            value = transfer.coins,
            ihrFee = Coins(),
            fwdFee = Coins(),
            createdLt = 0u,
            createdAt = 0u
        )
        val init = Maybe.of(transfer.stateInit?.let {
            Either.of<StateInit, CellRef<StateInit>>(null, CellRef(it))
        })
        val bodyCell = transfer.body
        val body = if (bodyCell == null || bodyCell.isEmpty()) {
            Either.of<Cell, CellRef<Cell>>(Cell.empty(), null)
        } else {
            Either.of<Cell, CellRef<Cell>>(null, CellRef(bodyCell))
        }

        return MessageRelaxed(
            info = info,
            init = init,
            body = body,
        )
    }
}