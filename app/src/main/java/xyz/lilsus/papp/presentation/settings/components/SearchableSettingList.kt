package xyz.lilsus.papp.presentation.settings.components

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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import xyz.lilsus.papp.R
import xyz.lilsus.papp.presentation.model.SettingOption


@Composable
fun SearchableSettingList(
    title: String,
    options: List<SettingOption>,
    selectedTag: String,
    onOptionSelected: (String) -> Unit,
    searchQuery: String,
    onSearchQueryChanged: (String) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(topBar = { Bar(title, onBack) }) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            TextField(
                value = searchQuery,
                onValueChange = onSearchQueryChanged,
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
                val filteredOptions = options.filter {
                    it.displayName.startsWith(searchQuery, ignoreCase = true)
                }
                items(filteredOptions) { option ->
                    SettingListItem(
                        option = option,
                        isSelected = option.tag == selectedTag,
                        onClick = { onOptionSelected(option.tag) }
                    )
                }
            }
        }
    }
}

@Composable
private fun SettingListItem(
    option: SettingOption,
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
