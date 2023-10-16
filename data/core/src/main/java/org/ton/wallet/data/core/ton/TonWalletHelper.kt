package org.ton.wallet.data.core.ton

import kotlinx.datetime.Clock
import org.ton.api.pk.PrivateKeyEd25519
import org.ton.bitstring.BitString
import org.ton.block.*
import org.ton.boc.BagOfCells
import org.ton.cell.Cell
import org.ton.cell.CellBuilder
import org.ton.contract.wallet.WalletTransfer
import org.ton.hashmap.HmeEmpty
import org.ton.tlb.*
import org.ton.tlb.constructor.AnyTlbConstructor
import org.ton.wallet.core.ext.toHexByteArray
import org.ton.wallet.data.core.model.TonAccount
import org.ton.wallet.data.core.model.TonAccountType
import kotlin.time.Duration.Companion.seconds

object TonWalletHelper {

    private const val ContractWalletV3R1 = "b5ee9c720101010100620000c0ff0020dd2082014c97ba9730ed44d0d70b1fe0a4f2608308d71820d31fd31fd31ff82313bbf263ed44d0d31fd31fd3ffd15132baf2a15144baf2a204f901541055f910f2a3f8009320d74a96d307d402fb00e8d101a4c8cb1fcb1fcbffc9ed54"
    private const val ContractWalletV3R2 = "b5ee9c720101010100710000deff0020dd2082014c97ba218201339cbab19f71b0ed44d0d31fd31f31d70bffe304e0a4f2608308d71820d31fd31fd31ff82313bbf263ed44d0d31fd31fd3ffd15132baf2a15144baf2a204f901541055f910f2a3f8009320d74a96d307d402fb00e8d101a4c8cb1fcb1fcbffc9ed54"
    private const val ContractWalletV4R2 = "b5ee9c72410214010002d4000114ff00f4a413f4bcf2c80b010201200203020148040504f8f28308d71820d31fd31fd31f02f823bbf264ed44d0d31fd31fd3fff404d15143baf2a15151baf2a205f901541064f910f2a3f80024a4c8cb1f5240cb1f5230cbff5210f400c9ed54f80f01d30721c0009f6c519320d74a96d307d402fb00e830e021c001e30021c002e30001c0039130e30d03a4c8cb1f12cb1fcbff1011121302e6d001d0d3032171b0925f04e022d749c120925f04e002d31f218210706c7567bd22821064737472bdb0925f05e003fa403020fa4401c8ca07cbffc9d0ed44d0810140d721f404305c810108f40a6fa131b3925f07e005d33fc8258210706c7567ba923830e30d03821064737472ba925f06e30d06070201200809007801fa00f40430f8276f2230500aa121bef2e0508210706c7567831eb17080185004cb0526cf1658fa0219f400cb6917cb1f5260cb3f20c98040fb0006008a5004810108f45930ed44d0810140d720c801cf16f400c9ed540172b08e23821064737472831eb17080185005cb055003cf1623fa0213cb6acb1fcb3fc98040fb00925f03e20201200a0b0059bd242b6f6a2684080a06b90fa0218470d4080847a4937d29910ce6903e9ff9837812801b7810148987159f31840201580c0d0011b8c97ed44d0d70b1f8003db29dfb513420405035c87d010c00b23281f2fff274006040423d029be84c600201200e0f0019adce76a26840206b90eb85ffc00019af1df6a26840106b90eb858fc0006ed207fa00d4d422f90005c8ca0715cbffc9d077748018c8cb05cb0222cf165005fa0214cb6b12ccccc973fb00c84014810108f451f2a7020070810108d718fa00d33fc8542047810108f451f2a782106e6f746570748018c8cb05cb025006cf165004fa0214cb6a12cb1fcb3fc973fb0002006c810108d718fa00d33f305224810108f459f2a782106473747270748018c8cb05cb025005cf165003fa0213cb6acb1f12cb3fc973fb00000af400c9ed54696225e5"

    fun getContractCode(accountType: TonAccountType): ByteArray {
        val codeString = when (accountType) {
            TonAccountType.v3r1 -> ContractWalletV3R1
            TonAccountType.v3r2 -> ContractWalletV3R2
            TonAccountType.v4r2 -> ContractWalletV4R2
        }
        return codeString.toHexByteArray()
    }

    fun getStateInitBytes(account: TonAccount): ByteArray {
        val stateInit = getStateInit(account)
        val stateInitCell = CellBuilder()
            .storeTlb(StateInit.tlbCodec(), stateInit)
            .endCell()
        return BagOfCells(stateInitCell).toByteArray()
    }

    fun getAccountData(account: TonAccount): ByteArray {
        val rootCellBuilder = CellBuilder()
            .storeUInt(account.seqNo, 32)
            .storeUInt(account.subWalletId, 32)
            .storeBytes(account.getPublicKeyBytes())
        if (account.version == 4) {
            rootCellBuilder.storeBit(false)
        }
        return BagOfCells(rootCellBuilder.endCell()).toByteArray()
    }

    fun getTransferMessageBody(
        fromAccount: TonAccount,
        toWorkChainId: Int,
        toRawAddress: ByteArray,
        amount: Long,
        message: String? = null,
        seed: ByteArray? = null
    ): ByteArray {
        val bodyCell =
            if (message.isNullOrEmpty()) null
            else MessageData.text(message).body
        val transfer = WalletTransfer {
            destination = AddrStd(toWorkChainId, toRawAddress)
            coins = Coins.ofNano(amount)
            body = bodyCell
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

    private fun getStateInit(account: TonAccount): StateInit {
        val code = getContractCode(account.type)
        val publicKey = account.getPublicKeyBytes()
        return StateInit(
            code = BagOfCells(code).roots.first(),
            data = CellBuilder.createCell {
                storeUInt(0, 32)
                storeBits(BitString(publicKey))
            },
            library = HmeEmpty(),
            splitDepth = null,
            special = null,
        )
    }
}