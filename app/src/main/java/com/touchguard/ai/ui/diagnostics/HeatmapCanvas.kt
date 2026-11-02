package com.touchguard.ai.ui.diagnostics

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.touchguard.ai.ui.theme.DarkNavyBackground
import com.touchguard.ai.ui.theme.NeonAmber
import com.touchguard.ai.ui.theme.NeonCyan
import com.touchguard.ai.ui.theme.NeonDanger
import com.touchguard.ai.ui.theme.TextPrimary
import com.touchguard.ai.viewmodel.DiagnosticsViewModel

@Composable
fun HeatmapCanvas(
    viewModel: DiagnosticsViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val points by viewModel.heatmapPoints.collectAsState()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DarkNavyBackground)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            points.forEach { (x, y) ->
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            NeonDanger.copy(alpha = 0.5f),
                            NeonAmber.copy(alpha = 0.25f),
                            NeonCyan.copy(alpha = 0.08f),
                            Color.Transparent
                        ),
                        center = Offset(x, y),
                        radius = 45.dp.toPx()
                    ),
                    radius = 45.dp.toPx(),
                    center = Offset(x, y)
                )
            }
        }

        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier.background(Color(0xCC070B19), RoundedCornerShape(8.dp))
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = TextPrimary)
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(
                modifier = Modifier
                    .background(Color(0xCC070B19), RoundedCornerShape(8.dp))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "Touch Density Heatmap (${points.size} points)",
                    color = NeonCyan,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Glow indicates high-frequency touch clusters",
                    color = Color.LightGray,
                    fontSize = 10.sp
                )
            }
            Spacer(modifier = Modifier.weight(1f))
            Button(
                onClick = { viewModel.clearHeatmap() },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xCC070B19)),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(Icons.Default.Delete, contentDescription = null, tint = NeonDanger)
                Spacer(modifier = Modifier.width(4.dp))
                Text("Clear", color = TextPrimary, fontSize = 11.sp)
            }
        }
    }
}
