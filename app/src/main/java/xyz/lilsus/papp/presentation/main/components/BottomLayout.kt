package xyz.lilsus.papp.presentation.main.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun BottomLayout(
    modifier: Modifier = Modifier.fillMaxWidth(),
    color: Color = MaterialTheme.colorScheme.primary,
    title: String,
    subtitle: String? = null
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            modifier = Modifier.padding(top = 16.dp),
            text = title,
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp,
                color = color
            )
        )
        subtitle?.let {
            Text(
                modifier = Modifier.padding(top = 8.dp),
                text = subtitle,
                style = MaterialTheme.typography.labelMedium
            )
        }

    }
}