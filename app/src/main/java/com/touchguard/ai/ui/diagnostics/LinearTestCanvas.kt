package com.touchguard.ai.ui.diagnostics

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
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
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.touchguard.ai.ui.theme.DarkNavyBackground
import com.touchguard.ai.ui.theme.NeonCyan
import com.touchguard.ai.ui.theme.NeonEmerald
import com.touchguard.ai.ui.theme.TextPrimary

@Composable
fun LineTestCanvas(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val drawnPoints = remember { mutableStateListOf<Offset>() }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DarkNavyBackground)
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = { offset ->
                            drawnPoints.clear()
                            drawnPoints.add(offset)
                        },
                        onDrag = { change, _ ->
                            change.consume()
                            drawnPoints.add(change.position)
                        }
                    )
                }
        ) {
            val w = size.width
            val h = size.height

            // Guide Target Lines (Horizontal & Vertical cross lines)
            val guideColor = Color(0xFF1E293B)
            for (step in 1..4) {
                val y = (h / 5f) * step
                drawLine(guideColor, Offset(0f, y), Offset(w, y), strokeWidth = 2f)
            }
            for (step in 1..3) {
                val x = (w / 4f) * step
                drawLine(guideColor, Offset(x, 0f), Offset(x, h), strokeWidth = 2f)
            }

            // Diagonal guides
            drawLine(Color(0xFF0F1E36), Offset(0f, 0f), Offset(w, h), strokeWidth = 2f)
            drawLine(Color(0xFF0F1E36), Offset(w, 0f), Offset(0f, h), strokeWidth = 2f)

            // User Drawn Path
            if (drawnPoints.size > 1) {
                val path = Path().apply {
                    moveTo(drawnPoints[0].x, drawnPoints[0].y)
                    for (i in 1 until drawnPoints.size) {
                        lineTo(drawnPoints[i].x, drawnPoints[i].y)
                    }
                }
                drawPath(
                    path = path,
                    color = NeonEmerald,
                    style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round)
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
                    text = "Linearity & Continuity Guide",
                    color = NeonCyan,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Draw along horizontal/diagonal lines. Watch for breaks or jumps.",
                    color = Color.LightGray,
                    fontSize = 10.sp
                )
            }
        }
    }
}
