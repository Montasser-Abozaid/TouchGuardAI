package com.touchguard.ai.ui.dashboard

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.touchguard.ai.ui.theme.NeonCyan
import com.touchguard.ai.ui.theme.NeonCyanGlow
import com.touchguard.ai.ui.theme.NeonDanger

@Composable
fun ActivityGraph(
    history: List<Float>,
    modifier: Modifier = Modifier
) {
    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(70.dp)
    ) {
        if (history.isEmpty()) return@Canvas

        val w = size.width
        val h = size.height
        val stepX = w / (history.size - 1).coerceAtLeast(1)

        val path = Path()
        val fillPath = Path()

        history.forEachIndexed { i, value ->
            val x = i * stepX
            val y = h - (value * (h - 12f)) - 6f
            if (i == 0) {
                path.moveTo(x, y)
                fillPath.moveTo(x, h)
                fillPath.lineTo(x, y)
            } else {
                path.lineTo(x, y)
                fillPath.lineTo(x, y)
            }
        }

        fillPath.lineTo(w, h)
        fillPath.close()

        // Draw gradient area under curve
        drawPath(
            path = fillPath,
            brush = Brush.verticalGradient(
                colors = listOf(NeonCyanGlow, Color.Transparent)
            )
        )

        // Draw line curve
        drawPath(
            path = path,
            color = NeonCyan,
            style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round)
        )

        // Draw active dots for anomaly spikes
        history.forEachIndexed { i, value ->
            if (value > 0.6f) {
                val x = i * stepX
                val y = h - (value * (h - 12f)) - 6f
                drawCircle(
                    color = NeonDanger,
                    radius = 4.dp.toPx(),
                    center = androidx.compose.ui.geometry.Offset(x, y)
                )
            }
        }
    }
}
