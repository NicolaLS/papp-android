package xyz.lilsus.papp.di

import android.content.Context
import android.os.Vibrator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import xyz.lilsus.papp.data.WalletConfigStoreSerializer.walletConfigStore
import xyz.lilsus.papp.data.android.SimpleLocaleProvider
import xyz.lilsus.papp.data.repository.CoinGeckoExchangeRateRepository
import xyz.lilsus.papp.data.repository.SettingsRepositoryImpl
import xyz.lilsus.papp.data.repository.WalletConfigRepositoryImpl
import xyz.lilsus.papp.data.repository.WalletRepositoryFactoryImpl
import xyz.lilsus.papp.data.settingsDataStore
import xyz.lilsus.papp.domain.android.LocaleProvider
import xyz.lilsus.papp.domain.repository.WalletRepository
import xyz.lilsus.papp.domain.use_case.amount.GetFormatterFlowUseCase
import xyz.lilsus.papp.domain.use_case.exchange.GetExchangeRateFlowUseCase

class AppDependencies(context: Context, private val applicationScope: CoroutineScope) {
    val walletConfigRepository = WalletConfigRepositoryImpl(context.walletConfigStore)
    val settingsRepository = SettingsRepositoryImpl(context.settingsDataStore)

    // Exchange Rate Repository (CoinGecko)
    val exchangeRateRepository = CoinGeckoExchangeRateRepository()

    // Use cases for currency/amount handling
    val getExchangeRateFlowUseCase = GetExchangeRateFlowUseCase(
        exchangeRateRepository = exchangeRateRepository,
        applicationScope = applicationScope,
        pollingIntervalMs = 10_000L,
    )
    val localeProvider: LocaleProvider = SimpleLocaleProvider()
    val getFormatterFlowUseCase =
        GetFormatterFlowUseCase(settingsRepository, localeProvider, getExchangeRateFlowUseCase)

    private val walletRepositoryFactory = WalletRepositoryFactoryImpl()

    // FIXME: This is kind of stupid
    // Expose a StateFlow of the WalletRepository (auto-updating client)
    val walletRepositoryFlow: StateFlow<WalletRepository?> =
        walletConfigRepository.activeWalletConfigOrNull
            .map { it?.let { walletRepositoryFactory.getClientFromConfigOrNull(it) } }
            .stateIn(
                scope = applicationScope,
                started = kotlinx.coroutines.flow.SharingStarted.Eagerly,
                initialValue = null
            )

    val vibrator: Vibrator? = context.getSystemService(Vibrator::class.java)
}
