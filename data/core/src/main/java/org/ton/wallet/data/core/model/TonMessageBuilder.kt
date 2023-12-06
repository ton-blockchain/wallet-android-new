package org.ton.wallet.data.core.model

import kotlinx.datetime.Clock
import org.ton.api.pk.PrivateKeyEd25519
import org.ton.bitstring.BitString
import org.ton.block.*
import org.ton.cell.Cell
import org.ton.cell.CellBuilder
import org.ton.contract.wallet.WalletTransfer
import org.ton.tlb.CellRef
import org.ton.tlb.storeTlb
import org.ton.wallet.data.core.ton.MessageText
import kotlin.time.Duration.Companion.seconds

object TonMessageBuilder {

    fun buildExternalMessage(destAddress: AddrStd, stateInit: StateInit?, cell: Cell): Message<Cell> {
        val info = ExtInMsgInfo(AddrNone, destAddress, Coins())
        val maybeStateInit = Maybe.of(stateInit?.let { Either.of<StateInit, CellRef<StateInit>>(null, CellRef(it)) })
        val body = Either.of<Cell, CellRef<Cell>>(null, CellRef(cell))
        return Message(info, maybeStateInit, body)
    }

    fun getTransactionCell(fromAccount: TonAccount, messages: List<MessageData>, seed: ByteArray? = null): Cell {
        val validUntil = (Clock.System.now() + 60.seconds).epochSeconds.toInt()
        val unsignedBody = CellBuilder.createCell {
            storeUInt(fromAccount.subWalletId, 32)
            storeUInt(validUntil, 32)
            storeUInt(fromAccount.seqNo, 32)
            if (fromAccount.type.version == 4) {
                storeUInt(0, 8) // op
            }
            for (msg in messages) {
                packMessage(this, msg)
            }
        }

        val signature = seed?.let(::PrivateKeyEd25519)?.let { BitString(it.sign(unsignedBody.hash())) }
        return CellBuilder.createCell {
            signature?.let { storeBits(it) }
            storeBits(unsignedBody.bits)
            storeRefs(unsignedBody.refs)
        }
    }

    private fun packMessage(cellBuilder: CellBuilder, message: MessageData) {
        var transferBody: Cell? = null
        if (message is MessageData.Raw) {
            transferBody = message.body
        } else if (message is MessageData.Text) {
            val textCell = message.textCell
            if (textCell != null) {
                transferBody = CellBuilder.createCell {
                    storeUInt(0, 32)
                    storeTlb(MessageText, textCell.value)
                }
            }
        }

        val transfer = WalletTransfer {
            destination = AddrStd.parse(message.destination)
            bounceable = TonUtils.isBounceableAddress(message.destination)
            coins = Coins.ofNano(message.amount)
            body = transferBody
            stateInit = message.stateInit
            build()
        }

        var sendMode = MessageData.OrdinarySendMode
        if (message.sendMode != MessageData.DefaultSendMode) {
            sendMode = message.sendMode
        }

        cellBuilder.storeUInt(sendMode, 8)
        val intMsg = createIntMsg(transfer)
        cellBuilder.storeRef(CellBuilder.createCell {
            // store msg info
            storeTlb(CommonMsgInfoRelaxed.tlbCombinator(), intMsg)

            // store state init
            if (message.stateInit != null) {
                // Maybe: state init is exist
                storeBit(true)
                // Either: state init should be wrapped in CellRef
                storeBit(true)
                // state init
                storeRef(CellBuilder.createCell {
                    storeTlb(StateInit.tlbCodec(), message.stateInit!!)
                })
            } else {
                // Maybe: state init is null
                storeBit(false)
            }

            // store body
            if (transferBody == null || transferBody.isEmpty()) {
                // Either: body is null
                storeBit(false)
            } else {
                // Either: body should be wrapped in CellRef
                storeBit(true)
                // body
                storeRef(transferBody)
            }
        })
    }

    private fun createIntMsg(transfer: WalletTransfer): CommonMsgInfoRelaxed.IntMsgInfoRelaxed {
        return CommonMsgInfoRelaxed.IntMsgInfoRelaxed(
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
    }
}