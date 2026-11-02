package com.touchguard.ai.ui.diagnostics

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.material.icons.filled.Refresh
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
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.touchguard.ai.R
import com.touchguard.ai.ui.theme.DarkNavyBackground
import com.touchguard.ai.ui.theme.NeonCyan
import com.touchguard.ai.ui.theme.NeonEmerald
import com.touchguard.ai.ui.theme.TextPrimary
import com.touchguard.ai.viewmodel.DiagnosticsViewModel

@Composable
fun TouchGridCanvas(
    viewModel: DiagnosticsViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val touchedCells by viewModel.touchedGridCells.collectAsState()
    val coverage = viewModel.getGridCoveragePercentage()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DarkNavyBackground)
    ) {
        // Interactive Touch Grid Canvas
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTapGestures { offset ->
                        val cellW = size.width / viewModel.gridCols
                        val cellH = size.height / viewModel.gridRows
                        val col = (offset.x / cellW).toInt()
                        val row = (offset.y / cellH).toInt()
                        viewModel.markGridCell(col, row)
                    }
                }
                .pointerInput(Unit) {
                    detectDragGestures { change, _ ->
                        change.consume()
                        val cellW = size.width / viewModel.gridCols
                        val cellH = size.height / viewModel.gridRows
                        val col = (change.position.x / cellW).toInt()
                        val row = (change.position.y / cellH).toInt()
                        viewModel.markGridCell(col, row)
                    }
                }
        ) {
            val cellW = size.width / viewModel.gridCols
            val cellH = size.height / viewModel.gridRows

            for (r in 0 until viewModel.gridRows) {
                for (c in 0 until viewModel.gridCols) {
                    val index = r * viewModel.gridCols + c
                    val isTouched = touchedCells.contains(index)
                    val left = c * cellW
                    val top = r * cellH

                    if (isTouched) {
                        drawRect(
                            color = NeonEmerald.copy(alpha = 0.65f),
                            topLeft = Offset(left, top),
                            size = Size(cellW, cellH)
                        )
                    }

                    drawRect(
                        color = Color(0xFF1E293B),
                        topLeft = Offset(left, top),
                        size = Size(cellW, cellH),
                        style = Stroke(width = 1f)
                    )
                }
            }
        }

        // Overlay Header Bar with Coverage and Controls
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
                    text = "Coverage: $coverage%",
                    color = if (coverage == 100) NeonEmerald else NeonCyan,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Swipe everywhere to reveal dead spots",
                    color = Color.LightGray,
                    fontSize = 10.sp
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = { viewModel.resetGrid() },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xCC070B19)),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(Icons.Default.Refresh, contentDescription = null, tint = NeonCyan)
                Spacer(modifier = Modifier.width(4.dp))
                Text("Reset", color = TextPrimary, fontSize = 11.sp)
            }
        }
    }
}
