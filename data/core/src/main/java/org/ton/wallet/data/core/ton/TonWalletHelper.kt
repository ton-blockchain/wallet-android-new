package org.ton.wallet.data.core.ton

import kotlinx.datetime.Clock
import org.ton.api.pk.PrivateKeyEd25519
import org.ton.bitstring.BitString
import org.ton.block.*
import org.ton.boc.BagOfCells
import org.ton.cell.Cell
import org.ton.cell.CellBuilder
import org.ton.contract.wallet.WalletTransfer
import org.ton.crypto.base64
import org.ton.crypto.base64url
import org.ton.tlb.CellRef
import org.ton.tlb.storeTlb
import org.ton.wallet.data.core.model.TonAccount
import kotlin.experimental.and
import kotlin.experimental.xor
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
        messages: List<MessageData>,
        seed: ByteArray? = null
    ): Cell {
        val validUntil = (Clock.System.now() + 60.seconds).epochSeconds.toInt()
        val unsignedBody = CellBuilder.createCell {
            storeUInt(fromAccount.subWalletId, 32)
            storeUInt(validUntil, 32)
            storeUInt(fromAccount.seqNo, 32)
            if (fromAccount.version == 4) {
                storeUInt(0, 8) // op
            }

            packOutMessages(this, messages)
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
                    msgData.body?.let { body ->
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

    // TODO: refactor this
    private fun getIsBouncable(address: String): Boolean {
        // use non-bouncable for raw address
        if (address.contains(":")) {
            return false
        }

        val packet = io.ktor.utils.io.core.ByteReadPacket(
            try {
                base64url(address)
            } catch (e: Exception) {
                try {
                    base64(address)
                } catch (e: Exception) {
                    throw IllegalArgumentException("Can't parse address: $address", e)
                }
            }
        )

        var tag = packet.readByte()

        val BOUNCABLE = 0x11.toByte()
        val NON_BOUNCABLE = 0x51.toByte()
        val TEST_FLAG = 0x80.toByte()

        // remote test flag if exist
        if (tag and TEST_FLAG == TEST_FLAG) {
            tag = tag xor TEST_FLAG
        }

        return when (tag) {
            BOUNCABLE -> {
                true
            }
            NON_BOUNCABLE -> {
                false
            }
            else -> {
                throw IllegalArgumentException("Can't parse address: $address")
            }
        }
    }

    private fun packOutMessages(cellBuilder: CellBuilder, messages: List<MessageData>): CellBuilder {
        if (messages.isEmpty()) {
            return cellBuilder
        }

        val message = messages.first()
        val remainingMessages = messages.drop(1)

        var transferBody: Cell? = null
        if (message is MessageData.Raw) {
            transferBody = message.body
        } else if (message is MessageData.Text) {
            transferBody = CellBuilder.createCell {
                storeUInt(0, 32)
                storeTlb(MessageText.Companion, message.text.value)
            }
        }

        val transfer = WalletTransfer {
            destination = AddrStd.parse(message.destination)
            bounceable = getIsBouncable(message.destination)
            coins = Coins.ofNano(message.amount)
            body = transferBody
            stateInit = message.stateInit
            build()
        }

        // TODO: move 3 to constant 1 & 2 outside of this function
        var sendMode = 3
        if (message.sendMode > -1) {
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

        // store remaining messages
        packOutMessages(cellBuilder, remainingMessages)

        return cellBuilder
    }
}