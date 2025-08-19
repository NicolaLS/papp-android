package xyz.lilsus.papp.presentation.main.components

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import xyz.lilsus.papp.presentation.main.PaymentUiState

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun QrCodeBottomSheet(
    uiState: PaymentUiState,
    done: Boolean,
    onDismiss: () -> Unit,
    sheetState: SheetState
) {


    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
    ) {
        // Handle back button: block unless done
        BackHandler(enabled = !done) {}

        Column(
            modifier = Modifier
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

                else -> {} // Idle: don't show anything
            }

        }
    }
}