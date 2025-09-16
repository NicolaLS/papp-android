package xyz.lilsus.papp.presentation.settings.screens.currency

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import xyz.lilsus.papp.R
import xyz.lilsus.papp.common.Constants
import xyz.lilsus.papp.presentation.model.SettingOption
import xyz.lilsus.papp.presentation.settings.components.SearchableSettingList

@Composable
fun Currency(
    modifier: Modifier = Modifier,
    viewModel: CurrencyViewModel,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.refresh()
    }

    val options = Constants.SUPPORTED_CURRENCY_CODES.map { code ->
        val name = when (code) {
            "SAT" -> stringResource(R.string.currency_sat)
            "BTC" -> stringResource(R.string.currency_btc)
            "USD" -> stringResource(R.string.currency_usd)
            "EUR" -> stringResource(R.string.currency_eur)
            "GBP" -> stringResource(R.string.currency_gbp)
            "CAD" -> stringResource(R.string.currency_cad)
            "AUD" -> stringResource(R.string.currency_aud)
            "CHF" -> stringResource(R.string.currency_chf)
            "JPY" -> stringResource(R.string.currency_jpy)
            "CNY" -> stringResource(R.string.currency_cny)
            "INR" -> stringResource(R.string.currency_inr)
            else -> code
        }
        SettingOption(displayName = name, tag = code)
    }

    SearchableSettingList(
        title = stringResource(R.string.currency),
        options = options,
        selectedTag = uiState.selectedCurrencyTag,
        onOptionSelected = viewModel::onCurrencySelected,
        searchQuery = uiState.searchQuery,
        onSearchQueryChanged = viewModel::onSearchQueryChanged,
        onBack = onBack,
        modifier = modifier
    )
}
