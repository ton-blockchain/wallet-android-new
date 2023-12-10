package org.ton.wallet.domain.transactions.impl

import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import org.ton.wallet.core.Res
import org.ton.wallet.core.ext.addSpans
import org.ton.wallet.coreui.Formatter
import org.ton.wallet.coreui.util.FontSpan
import org.ton.wallet.data.core.ton.TonCoin
import org.ton.wallet.data.core.util.LoadType
import org.ton.wallet.data.settings.api.SettingsRepository
import org.ton.wallet.data.transactions.api.TransactionsRepository
import org.ton.wallet.data.transactions.api.model.TransactionDto
import org.ton.wallet.data.wallet.api.AccountsRepository
import org.ton.wallet.data.wallet.api.model.AccountDto
import org.ton.wallet.domain.blockhain.api.AddressType
import org.ton.wallet.domain.blockhain.api.GetAddressTypeUseCase
import org.ton.wallet.domain.transactions.api.GetTransactionsUseCase
import org.ton.wallet.domain.transactions.api.model.*
import org.ton.wallet.strings.RPlurals
import org.ton.wallet.strings.RString
import org.ton.wallet.uikit.RUiKitColor
import org.ton.wallet.uikit.RUiKitFont
import java.math.BigDecimal
import java.util.Calendar

class GetTransactionsUseCaseImpl(
    private val accountsRepository: AccountsRepository,
    private val settingsRepository: SettingsRepository,
    private val transactionsRepository: TransactionsRepository,
    private val getAddressTypeUseCase: GetAddressTypeUseCase
) : GetTransactionsUseCase {

    private val calendar by lazy { Calendar.getInstance() }
    private val colorPositive = Res.color(RUiKitColor.text_approve)
    private val colorNegative = Res.color(RUiKitColor.text_error)

    override suspend fun invoke(isReload: Boolean): List<TransactionBaseUiListItem>? {
        val accountType = settingsRepository.accountTypeFlow.value ?: return null
        val accountAddress = accountsRepository.getAccountAddress(accountType) ?: return null
        val loadType = if (isReload) LoadType.OnlyApi else LoadType.OnlyCache
        var account: AccountDto? = null
        if (isReload) {
            account = accountsRepository.fetchAccountState(accountAddress, accountType)
        }
        if (account == null) {
            account = accountsRepository.getAccountStateFlow(accountAddress).value ?: return null
        }
        val transactions = transactionsRepository.getTransactions(account, loadType)
        return mapTransactions(transactions)
    }

    private suspend fun mapTransactions(transactions: List<TransactionDto>?): List<TransactionBaseUiListItem>? {
        if (transactions == null) {
            return null
        }
        val items = mutableListOf<TransactionBaseUiListItem>()
        for (i in transactions.indices) {
            val currentDto = transactions[i]
            val dtoTimestampMs = currentDto.timestampSec?.times(1000L) ?: 0L

            var isNeedDateItem: Boolean
            if (i == 0) {
                isNeedDateItem = true
            } else {
                val previousDtoTimestampMs = transactions[i - 1].timestampSec?.times(1000L) ?: 0L
                calendar.timeInMillis = previousDtoTimestampMs
                val previousDay = calendar.get(Calendar.DAY_OF_YEAR)
                val previousYear = calendar.get(Calendar.YEAR)

                calendar.timeInMillis = dtoTimestampMs
                val currentDay = calendar.get(Calendar.DAY_OF_YEAR)
                val currentYear = calendar.get(Calendar.YEAR)
                isNeedDateItem = previousDay != currentDay || previousYear != currentYear
            }

            if (isNeedDateItem) {
                val dateString = Formatter.getDayMonthString(dtoTimestampMs)
                val dateItem = TransactionHeaderUiListItem(dateString)
                items.add(dateItem)
            }
            items.add(mapDtoToItem(currentDto))
        }
        return items
    }

    private suspend fun mapDtoToItem(dto: TransactionDto): TransactionDataUiListItem {
        var valueCharSequence: CharSequence? = null
        val amount = dto.getTotalAmount()
        if (amount != null) {
            val valueString = Formatter.getFormattedAmount(amount)
            val valueBuilder = Formatter.getBeautifiedAmount(valueString, proportion = 0.77f)
            val color = if (amount > 0) colorPositive else colorNegative
            valueBuilder?.setSpan(ForegroundColorSpan(color), 0, valueBuilder.length, Spannable.SPAN_INCLUSIVE_EXCLUSIVE)
            valueCharSequence = valueBuilder
        }

        var peerAddress = dto.getFirstAddress()
        var peerAddressBuilder: SpannableStringBuilder? = null
        if (peerAddress != null) {
            val peerAddressType = getAddressTypeUseCase.getAddressType(peerAddress)
            if (peerAddressType is AddressType.RawAddress) {
                peerAddress = peerAddressType.ufAddress
            }

            peerAddressBuilder = Formatter.getBeautifiedShortStringSafe(Formatter.getMiddleAddress(peerAddress), Res.font(RUiKitFont.roboto_regular))
            val outMessagesCount = dto.outMessages?.size ?: 0
            if (outMessagesCount > 1 && peerAddressBuilder != null) {
                val oldAddressLength = peerAddressBuilder.length
                val addressesString = Res.plural(RPlurals.addresses, outMessagesCount - 1, outMessagesCount - 1)
                val addressesResultString = "  +$addressesString"
                peerAddressBuilder = peerAddressBuilder.append(addressesResultString)
                val spans = listOf(
                    ForegroundColorSpan(Res.color(RUiKitColor.text_secondary)),
                    FontSpan(Res.font(RUiKitFont.roboto_regular))
                )
                peerAddressBuilder.addSpans(spans, oldAddressLength, peerAddressBuilder.length, Spannable.SPAN_INCLUSIVE_INCLUSIVE)
            }
        }

        var feeString: String? = null
        val storageFee = dto.storageFee
        if (storageFee != null && storageFee > 0L) {
            var feeDecimal = BigDecimal(storageFee)
            feeDecimal = feeDecimal.movePointLeft(TonCoin.Decimals).stripTrailingZeros()
            feeString = Res.str(RString.fee_storage, feeDecimal.toPlainString())
        }

        val timestampSec = dto.timestampSec
        var timeString: String? = null
        if (timestampSec != null && timestampSec != 0L) {
            timeString = Formatter.getTimeString(timestampSec * 1000)
        }

        return TransactionDataUiListItem(
            internalId = dto.internalId,
            type = dto.type,
            value = valueCharSequence,
            peerAddressShort = peerAddressBuilder,
            timeString = timeString,
            feeString = feeString,
            messageText = dto.getMessage()
        )
    }
}