package org.ton.wallet.data.core.ton

import kotlinx.datetime.Clock
import org.ton.api.pk.PrivateKeyEd25519
import org.ton.bitstring.BitString
import org.ton.block.*
import org.ton.boc.BagOfCells
import org.ton.cell.Cell
import org.ton.cell.CellBuilder
import org.ton.contract.wallet.WalletTransfer
import org.ton.tlb.CellRef
import org.ton.tlb.constructor.AnyTlbConstructor
import org.ton.tlb.storeRef
import org.ton.wallet.data.core.model.TonAccount
import kotlin.time.Duration.Companion.seconds

object TonWalletHelper {

    fun getTransferMessageBody(
        fromAccount: TonAccount,
        toWorkChainId: Int,
        toRawAddress: ByteArray,
        amount: Long,
        msgData: MessageData? = null,
        seed: ByteArray? = null
    ): ByteArray {
        val transfer = WalletTransfer {
            destination = AddrStd(toWorkChainId, toRawAddress)
            coins = Coins.ofNano(amount)
            body = msgData?.body
        }

        val unsignedBody = CellBuilder.createCell {
            storeUInt(fromAccount.subWalletId, 32)
            storeUInt((Clock.System.now() + 60.seconds).epochSeconds.toInt(), 32)
            storeUInt(fromAccount.seqNo, 32)
            storeUInt(0, 8) // op
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

        val msgCell = CellBuilder.createCell {
            signature?.let { storeBits(it) }
            storeBits(unsignedBody.bits)
            storeRefs(unsignedBody.refs)
        }
        return BagOfCells(msgCell).toByteArray()
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
        val body = if (transfer.body == null) {
            Either.of<Cell, CellRef<Cell>>(Cell.empty(), null)
        } else {
            Either.of<Cell, CellRef<Cell>>(null, CellRef(transfer.body!!))
        }

        return MessageRelaxed(
            info = info,
            init = init,
            body = body,
        )
    }
}