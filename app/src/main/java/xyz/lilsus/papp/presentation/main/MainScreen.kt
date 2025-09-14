package xyz.lilsus.papp.presentation.main

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LocalLifecycleOwner
import xyz.lilsus.papp.R
import xyz.lilsus.papp.presentation.ext.tapToDismiss
import xyz.lilsus.papp.presentation.main.components.BottomLayout
import xyz.lilsus.papp.presentation.main.components.BottomSheetOverlay
import xyz.lilsus.papp.presentation.main.components.PaymentResultScreen
import xyz.lilsus.papp.presentation.main.components.hero.Hero


@Composable
fun MainScreen(viewModel: MainViewModel, onSettingsClick: () -> Unit) {
    val lifecycleOwner = LocalLifecycleOwner.current
    val context = LocalContext.current

    val uiState = viewModel.uiState
    var paymentResult = remember { mutableStateOf<PaymentResult?>(null) }

    LaunchedEffect(uiState) {
        if (uiState is UiState.PaymentDone) {
            paymentResult.value = uiState.result
        }
    }

    LaunchedEffect(context, lifecycleOwner) {
        viewModel.dispatch(
            Intent.BindCamera(
                context,
                lifecycleOwner
            )
        )
    }

    BottomSheetOverlay(
        uiState = uiState,
        onAction = viewModel::dispatch
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .systemBarsPadding()
            .tapToDismiss(
                enabled = uiState is UiState.PaymentDone,
                onDismiss = { viewModel.dispatch(Intent.Dismiss) }
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Hero(
                modifier = Modifier.fillMaxWidth(),
                uiState = uiState,
            )
            Crossfade(targetState = uiState is UiState.PaymentDone) { showPaymentResultLayout ->
                when (showPaymentResultLayout) {
                    true -> {
                        val currentPaymentResult = paymentResult.value
                        Column(
                            modifier = Modifier
                                .fillMaxSize(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            currentPaymentResult?.let { PaymentResultScreen(currentPaymentResult) }
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = stringResource(R.string.tap_continue),
                                style = MaterialTheme.typography.labelLarge
                            )
                        }

                    }

                    false -> {
                        BottomLayout(
                            title = stringResource(R.string.app_name_long),
                            subtitle = stringResource(R.string.point_camera_message_subtitle)
                        )
                    }
                }
            }
        }
        // TODO: i18n meta strings e.g contentDescription here.
        ExtendedFloatingActionButton(
            onClick = onSettingsClick,
            icon = { Icon(Icons.Filled.Settings, "Extended floating action button.") },
            text = { Text(text = stringResource(R.string.settings)) },
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(16.dp),
        )
    }
}