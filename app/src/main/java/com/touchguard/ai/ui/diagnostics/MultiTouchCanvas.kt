package com.touchguard.ai.ui.diagnostics

import android.view.MotionEvent
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
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.touchguard.ai.ui.theme.DarkNavyBackground
import com.touchguard.ai.ui.theme.NeonCyan
import com.touchguard.ai.ui.theme.NeonEmerald
import com.touchguard.ai.ui.theme.TextPrimary
import com.touchguard.ai.viewmodel.DiagnosticsViewModel

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun MultiTouchCanvas(
    viewModel: DiagnosticsViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val activePointers by viewModel.activePointers.collectAsState()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DarkNavyBackground)
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInteropFilter { motionEvent ->
                    when (motionEvent.actionMasked) {
                        MotionEvent.ACTION_DOWN, MotionEvent.ACTION_POINTER_DOWN, MotionEvent.ACTION_MOVE -> {
                            for (i in 0 until motionEvent.pointerCount) {
                                val id = motionEvent.getPointerId(i)
                                val x = motionEvent.getX(i)
                                val y = motionEvent.getY(i)
                                val pressure = motionEvent.getPressure(i)
                                val size = motionEvent.getSize(i)
                                viewModel.updatePointer(id, x, y, pressure, size)
                            }
                            true
                        }
                        MotionEvent.ACTION_POINTER_UP -> {
                            val actionIdx = motionEvent.actionIndex
                            val id = motionEvent.getPointerId(actionIdx)
                            viewModel.removePointer(id)
                            true
                        }
                        MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                            viewModel.clearAllPointers()
                            true
                        }
                        else -> false
                    }
                }
        ) {
            activePointers.values.forEach { track ->
                // Outer ring
                drawCircle(
                    color = track.color.copy(alpha = 0.3f),
                    radius = 55.dp.toPx(),
                    center = Offset(track.x, track.y)
                )
                drawCircle(
                    color = track.color,
                    radius = 35.dp.toPx(),
                    center = Offset(track.x, track.y),
                    style = Stroke(width = 2.5.dp.toPx())
                )
                // Center point
                drawCircle(
                    color = Color.White,
                    radius = 6.dp.toPx(),
                    center = Offset(track.x, track.y)
                )
                // Crosshairs
                drawLine(
                    color = track.color.copy(alpha = 0.5f),
                    start = Offset(track.x - 60f, track.y),
                    end = Offset(track.x + 60f, track.y),
                    strokeWidth = 1.5f
                )
                drawLine(
                    color = track.color.copy(alpha = 0.5f),
                    start = Offset(track.x, track.y - 60f),
                    end = Offset(track.x, track.y + 60f),
                    strokeWidth = 1.5f
                )
            }
        }

        // Header Telemetry
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
                    text = "Active Pointers: ${activePointers.size} / 10",
                    color = if (activePointers.size > 2) NeonEmerald else NeonCyan,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                if (activePointers.isNotEmpty()) {
                    val first = activePointers.values.first()
                    Text(
                        text = "P0: (${first.x.toInt()}, ${first.y.toInt()}) Prs: ${String.format("%.2f", first.pressure)}",
                        color = Color.LightGray,
                        fontSize = 10.sp
                    )
                }
            }
        }
    }
}
