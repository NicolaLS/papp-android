package xyz.lilsus.papp.presentation.settings.screens.currency

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import xyz.lilsus.papp.R
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

    SearchableSettingList(
        title = stringResource(R.string.currency),
        options = uiState.currencies,
        selectedTag = uiState.selectedCurrencyTag,
        onOptionSelected = viewModel::onCurrencySelected,
        searchQuery = uiState.searchQuery,
        onSearchQueryChanged = viewModel::onSearchQueryChanged,
        onBack = onBack,
        modifier = modifier
    )
}
