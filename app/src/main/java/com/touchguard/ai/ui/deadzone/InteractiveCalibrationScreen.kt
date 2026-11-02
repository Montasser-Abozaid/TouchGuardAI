package com.touchguard.ai.ui.deadzone

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.touchguard.ai.core.model.DeadZoneMode
import com.touchguard.ai.ui.theme.DarkNavyBackground
import com.touchguard.ai.ui.theme.ElectricBlue
import com.touchguard.ai.ui.theme.NeonCyan
import com.touchguard.ai.ui.theme.NeonDanger
import com.touchguard.ai.ui.theme.TextPrimary
import com.touchguard.ai.ui.theme.TextSecondary
import com.touchguard.ai.viewmodel.DeadZoneViewModel

@Composable
fun InteractiveCalibrationScreen(
    viewModel: DeadZoneViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val leftRatio by viewModel.draftLeftRatio.collectAsState()
    val topRatio by viewModel.draftTopRatio.collectAsState()
    val rightRatio by viewModel.draftRightRatio.collectAsState()
    val bottomRatio by viewModel.draftBottomRatio.collectAsState()

    var zoneName by remember { mutableStateOf("Damaged Zone 1") }
    var selectedMode by remember { mutableStateOf(DeadZoneMode.BLOCK_TOUCH) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DarkNavyBackground)
    ) {
        // Drag Canvas
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectDragGestures { change, _ ->
                        change.consume()
                        val posX = change.position.x / size.width
                        val posY = change.position.y / size.height

                        // If drag is closer to top-left or bottom-right
                        val distToTL = kotlin.math.hypot(posX - leftRatio, posY - topRatio)
                        val distToBR = kotlin.math.hypot(posX - rightRatio, posY - bottomRatio)

                        if (distToTL < distToBR) {
                            viewModel.updateDraftBounds(posX, posY, rightRatio, bottomRatio)
                        } else {
                            viewModel.updateDraftBounds(leftRatio, topRatio, posX, posY)
                        }
                    }
                }
        ) {
            val w = size.width
            val h = size.height

            val left = leftRatio * w
            val top = topRatio * h
            val right = rightRatio * w
            val bottom = bottomRatio * h
            val boxW = right - left
            val boxH = bottom - top

            // Shaded Dead Zone Area
            drawRect(
                color = NeonDanger.copy(alpha = 0.35f),
                topLeft = Offset(left, top),
                size = Size(boxW, boxH)
            )
            // Border
            drawRect(
                color = NeonDanger,
                topLeft = Offset(left, top),
                size = Size(boxW, boxH),
                style = Stroke(width = 3.dp.toPx())
            )

            // Top-Left Handle
            drawCircle(
                color = NeonCyan,
                radius = 14.dp.toPx(),
                center = Offset(left, top)
            )
            // Bottom-Right Handle
            drawCircle(
                color = NeonCyan,
                radius = 14.dp.toPx(),
                center = Offset(right, bottom)
            )
        }

        // Top Navigation Bar
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
            Text(
                text = "DRAG CORNERS TO COVER FLAW",
                color = NeonCyan,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .background(Color(0xCC070B19), RoundedCornerShape(8.dp))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            )
        }

        // Bottom Configuration & Save Box
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(16.dp)
                .background(Color(0xEE0D1527), RoundedCornerShape(16.dp))
                .padding(16.dp)
        ) {
            Column {
                OutlinedTextField(
                    value = zoneName,
                    onValueChange = { zoneName = it },
                    label = { Text("Zone Name / Note") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedBorderColor = NeonCyan,
                        unfocusedBorderColor = Color(0xFF2A3B66)
                    ),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = { selectedMode = DeadZoneMode.BLOCK_TOUCH },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (selectedMode == DeadZoneMode.BLOCK_TOUCH) NeonDanger else Color(0xFF1E293B)
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("BLOCK TOUCH", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = { selectedMode = DeadZoneMode.MONITOR_ONLY },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (selectedMode == DeadZoneMode.MONITOR_ONLY) NeonCyan else Color(0xFF1E293B)
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            "MONITOR ONLY",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (selectedMode == DeadZoneMode.MONITOR_ONLY) DarkNavyBackground else TextPrimary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = {
                        viewModel.saveDraftAsDeadZone(zoneName, selectedMode)
                        onBack()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.Check, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("SAVE & ACTIVATE DEAD ZONE", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
        }
    }
}
