package xyz.lilsus.papp.presentation.main.components

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import xyz.lilsus.papp.presentation.main.PaymentUiState

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun QrCodeBottomSheet(
    uiState: PaymentUiState,
    onDismiss: () -> Unit
) {
    val done = remember { mutableStateOf(false) }

    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true,
        confirmValueChange = { newState ->
            // Allow dismiss only if done
            newState != SheetValue.Hidden || done.value
        }
    )

    // Start countdown when payment finishes
    LaunchedEffect(uiState) {
        if (uiState is PaymentUiState.Received || uiState is PaymentUiState.Error) {
            delay(1000) // prevent accidental dismiss
            done.value = true
        } else if (uiState is PaymentUiState.Loading) {
            done.value = false // reset when new payment starts
        }
    }



    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
    ) {
        // Handle back button: block unless done
        BackHandler(enabled = !done.value) {}

        Column(
            modifier = Modifier
                .fillMaxSize()
                .wrapContentSize(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            when (val state = uiState) {
                is PaymentUiState.Loading -> {
                    CircularProgressIndicator()
                }

                is PaymentUiState.Received -> {
                    PaymentResultScreen(state.result)
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(onClick = onDismiss) {
                        Text("Close")
                    }
                }

                is PaymentUiState.Error -> {
                    PaymentError(state.message)
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(onClick = onDismiss) {
                        Text("Close")
                    }
                }
            }

        }
    }
}