package xyz.lilsus.papp.presentation.settings.screens.language

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import xyz.lilsus.papp.R
import xyz.lilsus.papp.presentation.settings.components.Bar

@Composable
fun Language(
    modifier: Modifier = Modifier,
    viewModel: LanguageViewModel,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    // This effect runs when the composable enters the composition.
    // It will call the refresh function to ensure the language list and selection are up-to-date.
    LaunchedEffect(Unit) {
        viewModel.refresh()
    }

    Scaffold(topBar = { Bar(stringResource(R.string.language), onBack) }) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            TextField(
                value = uiState.searchQuery,
                onValueChange = viewModel::onSearchQueryChanged,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text(stringResource(R.string.search)) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search Icon") },
                singleLine = true,
                colors = TextFieldDefaults.colors(
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn {
                val filteredLanguages = uiState.languages.filter {
                    it.displayName.startsWith(uiState.searchQuery, ignoreCase = true)
                }
                items(filteredLanguages) { language ->
                    LanguageItem(
                        option = language,
                        isSelected = language.tag == uiState.selectedLanguageTag,
                        onClick = { viewModel.onLanguageSelected(language.tag) }
                    )
                }
            }
        }
    }
}

@Composable
private fun LanguageItem(
    option: LanguageOption,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = option.displayName, modifier = Modifier.weight(1f))
        if (isSelected) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "Selected",
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
    }
}
