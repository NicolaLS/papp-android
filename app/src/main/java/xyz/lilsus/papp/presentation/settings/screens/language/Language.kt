package xyz.lilsus.papp.presentation.settings.screens.language

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import xyz.lilsus.papp.R
import xyz.lilsus.papp.presentation.model.SettingOption
import xyz.lilsus.papp.presentation.settings.components.SearchableSettingList

@Composable
fun Language(
    modifier: Modifier = Modifier,
    viewModel: LanguageViewModel,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.refresh()
    }

    SearchableSettingList(
        title = stringResource(R.string.language),
        options = uiState.languages.map { SettingOption(it.displayName, it.tag) },
        selectedTag = uiState.selectedLanguageTag,
        onOptionSelected = viewModel::onLanguageSelected,
        searchQuery = uiState.searchQuery,
        onSearchQueryChanged = viewModel::onSearchQueryChanged,
        onBack = onBack,
        modifier = modifier
    )
}
