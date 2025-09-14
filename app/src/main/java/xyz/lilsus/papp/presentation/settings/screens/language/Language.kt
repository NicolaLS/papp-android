package xyz.lilsus.papp.presentation.settings.screens.language

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import xyz.lilsus.papp.R
import xyz.lilsus.papp.presentation.settings.components.Bar

@Composable
fun Language(modifier: Modifier = Modifier, onBack: () -> Unit) {
    Scaffold(topBar = { Bar(stringResource(R.string.language), onBack) }) { innerPadding ->
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Text("TBD.", modifier.align(Alignment.Center))
        }

    }
}
