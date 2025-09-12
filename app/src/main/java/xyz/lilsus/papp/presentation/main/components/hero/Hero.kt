package xyz.lilsus.papp.presentation.main.components.hero

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.tooling.preview.Preview
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import xyz.lilsus.papp.common.Invoice
import xyz.lilsus.papp.common.Resource
import xyz.lilsus.papp.domain.model.SendPaymentData
import xyz.lilsus.papp.domain.model.config.WalletTypeEntry
import xyz.lilsus.papp.domain.use_case.wallets.InvoiceConfirmationData
import xyz.lilsus.papp.presentation.main.PaymentResult
import xyz.lilsus.papp.presentation.main.UiState
import xyz.lilsus.papp.presentation.ui.theme.AppTheme

private val squares = listOf(
    SquareSpec(0.1f, 0.1f, 0.3f),
    SquareSpec(1f - (0.3f + 0.1f), 0.1f, 0.3f),
    SquareSpec(0.1f, 1f - (0.3f + 0.1f), 0.3f),
    SquareSpec(1f - (0.3f + 0.1f), 1f - (0.3f + 0.1f), 0.3f, false)
)

private val arcs = listOf(
    ArcSpec(0f, 0f, startAngle = 180f),
    ArcSpec(1f - 0.15f, 0f, startAngle = 270f),
    ArcSpec(0f, 1f - 0.15f, startAngle = 90f),
    ArcSpec(1f - 0.15f, 1f - 0.15f, startAngle = 0f)
)

@Composable
fun Hero(
    modifier: Modifier = Modifier,
    uiState: UiState,
) {
    val color = when (uiState) {
        UiState.Active -> MaterialTheme.colorScheme.onSurfaceVariant
        is UiState.QrDetected,
        is UiState.ConfirmPayment,
        UiState.PerformingPayment -> MaterialTheme.colorScheme.primary

        is UiState.PaymentDone -> when (uiState.result) {
            is PaymentResult.Success -> Color(0xFF4CAF50)
            is PaymentResult.Error -> MaterialTheme.colorScheme.error
        }
    }

    val animationState = rememberHeroAnimationState(squares, arcs)

    LaunchedEffect(uiState) {
        animationState.animateState(uiState, color)
    }

    Box(
        modifier = modifier
            .fillMaxHeight(0.5f)
            .fillMaxWidth()
            .animateContentSize(),
        contentAlignment = Alignment.Center
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth(0.6f)
                .aspectRatio(1f)
        ) {
            val canvasSize = size.minDimension
            val canvasCenter = Offset(size.width / 2f, size.height / 2f)

            scale(animationState.clusterScale, pivot = canvasCenter) {
                squares.forEachIndexed { index, spec ->
                    val px = (spec.x + animationState.squareOffsets[index].x) * canvasSize
                    val py = (spec.y + animationState.squareOffsets[index].y) * canvasSize

                    val s = spec.size * canvasSize
                    val size = Size(s, s)
                    val squareCenter = Offset(px + s / 2, py + s / 2)

                    val cornerRadius = CornerRadius(s * 0.1f)
                    val stroke = Stroke(width = s * 0.1f)

                    scale(animationState.squareScales[index], pivot = squareCenter) {
                        if (spec.outlined) {
                            drawRoundRect(
                                color = animationState.color,
                                size = size,
                                cornerRadius = cornerRadius,
                                topLeft = Offset(px, py),
                                style = stroke
                            )
                            val childSize = Size(size.width * 0.5f, size.height * 0.5f)
                            val offsetX = px + (size.width - childSize.width) / 2f
                            val offsetY = py + (size.height - childSize.height) / 2f
                            drawRoundRect(
                                color = animationState.color,
                                size = childSize,
                                cornerRadius = cornerRadius,
                                topLeft = Offset(offsetX, offsetY)
                            )
                        } else {
                            drawRoundRect(
                                color = animationState.color,
                                size = size,
                                cornerRadius = cornerRadius,
                                topLeft = Offset(px, py),
                            )
                        }
                    }
                }
            }

            arcs.forEachIndexed { index, spec ->
                val px = (spec.x + animationState.arcOffsets[index].x) * canvasSize
                val py = (spec.y + animationState.arcOffsets[index].y) * canvasSize
                val cornerLength = canvasSize * spec.cornerLength
                val cornerStroke = Stroke(width = canvasSize * 0.02f, cap = StrokeCap.Round)
                drawArc(
                    color = animationState.color,
                    startAngle = spec.startAngle,
                    sweepAngle = spec.sweepAngle,
                    useCenter = false,
                    style = cornerStroke,
                    size = Size(cornerLength, cornerLength),
                    topLeft = Offset(px, py)
                )
            }
        }
    }
}

@Preview
@Composable
private fun HeroPreview() {
    val bolt11Str =
        "lnbc10n1p5vg60spp579em8n8t2xk5vpqzrx6m0x5ywygd2rj9044zt4x8g4q3gcl2rmmsdqqcqzzsxqyz5vqsp594zaeqapuuklevng8svupu7vhjwkgy95jgmr6p6fnl23hejhhzqs9qxpqysgqamuwzlkxvf3dr0uwmyeue5tns83gth36zf6tajlafgawz7t6v4jj2urpskamtcyzqm3ykmlfma7m9cz89t89cqh7flg2hmxgr3xft8sp8zzsng"
    val invoice = Invoice.parse(bolt11Str)
    val confirmData = InvoiceConfirmationData(
        invoice as Invoice.Bolt11,
        flow { emit(Resource.Success(10L to WalletTypeEntry.BLINK)) }
    )
    val sendPaymentData = SendPaymentData.Success(
        amountPaid = 1000,
        feePaid = 10,
    )
    val states = listOf(
        UiState.Active,
        UiState.QrDetected(Invoice.Invalid.NotBolt11Invoice),
        UiState.ConfirmPayment(confirmData),
        UiState.PerformingPayment,
        UiState.PaymentDone(PaymentResult.Success(sendPaymentData to WalletTypeEntry.BLINK)),
        UiState.Active,
        UiState.QrDetected(Invoice.Invalid.NotBolt11Invoice),
        UiState.PaymentDone(PaymentResult.Error("")),
    )
    var currentState by remember { mutableStateOf<UiState>(UiState.Active) }
    LaunchedEffect(Unit) {
        while (true) {
            states.forEach {
                currentState = it
                delay(2000)
            }
        }
    }

    AppTheme {
        Hero(uiState = currentState)
    }
}
