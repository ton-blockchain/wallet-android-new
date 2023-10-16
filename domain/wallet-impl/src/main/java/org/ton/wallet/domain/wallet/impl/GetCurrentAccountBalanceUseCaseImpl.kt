package org.ton.wallet.domain.wallet.impl

import kotlinx.coroutines.flow.*
import org.ton.wallet.coreui.Formatter
import org.ton.wallet.data.core.model.FiatCurrency
import org.ton.wallet.data.core.ton.TonCoin
import org.ton.wallet.data.prices.api.PricesRepository
import org.ton.wallet.data.settings.api.SettingsRepository
import org.ton.wallet.domain.wallet.api.GetCurrentAccountBalanceUseCase
import org.ton.wallet.domain.wallet.api.GetCurrentAccountDataUseCase
import java.math.BigDecimal

class GetCurrentAccountBalanceUseCaseImpl(
    private val getCurrentAccountDataUseCase: GetCurrentAccountDataUseCase,
    private val pricesRepository: PricesRepository,
    private val settingsRepository: SettingsRepository,
) : GetCurrentAccountBalanceUseCase {

    override fun getTonBalanceFlow(): Flow<Long?> {
        return getCurrentAccountDataUseCase.getAccountStateFlow()
            .map { it.balance }
    }

    override fun getFiatBalanceStringFlow(): Flow<String> {
        return combine(
            getTonBalanceFlow(),
            settingsRepository.fiatCurrencyFlow
        ) { tonBalance, fiatCurrency ->
            tonBalance to fiatCurrency
        }.flatMapLatest { (balance: Long?, fiatCurrency: FiatCurrency) ->
            pricesRepository.getFiatPriceFlow(fiatCurrency)
                .map { fiatPrice ->
                    var fiatBalance = BigDecimal(balance ?: 0)
                    fiatBalance = fiatBalance.movePointLeft(TonCoin.Decimals)
                    fiatBalance = fiatBalance.multiply(BigDecimal.valueOf(fiatPrice))
                    val formattedAmount = Formatter.getFormattedAmount(fiatBalance, fiatCurrency.currencySymbol)
                    "≈ $formattedAmount"
                }
        }
    }
}