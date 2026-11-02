package com.touchguard.ai.ui.diagnostics

import android.view.MotionEvent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Dangerous
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.touchguard.ai.R
import com.touchguard.ai.core.model.AnomalySeverity
import com.touchguard.ai.core.model.GhostTouchAnomaly
import com.touchguard.ai.core.model.TouchCategory
import com.touchguard.ai.ui.theme.CardGlassNavy
import com.touchguard.ai.ui.theme.DarkNavyBackground
import com.touchguard.ai.ui.theme.GlassBorder
import com.touchguard.ai.ui.theme.NeonCyan
import com.touchguard.ai.ui.theme.NeonDanger
import com.touchguard.ai.ui.theme.NeonEmerald
import com.touchguard.ai.ui.theme.TextMuted
import com.touchguard.ai.ui.theme.TextPrimary
import com.touchguard.ai.ui.theme.TextSecondary
import com.touchguard.ai.viewmodel.DiagnosticsViewModel

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun AutonomousGhostDetectorScreen(
    viewModel: DiagnosticsViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isRunning by viewModel.isAutonomousRunning.collectAsState()
    val countdown by viewModel.autonomousCountdown.collectAsState()
    val detectedList by viewModel.detectedGhostTouches.collectAsState()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DarkNavyBackground)
            // Intercept touches on full screen to detect phantom spikes
            .pointerInteropFilter { ev ->
                if (isRunning && ev.actionMasked == MotionEvent.ACTION_DOWN) {
                    val anomaly = GhostTouchAnomaly(
                        timestamp = ev.eventTime,
                        x = ev.x,
                        y = ev.y,
                        anomalyScore = 100, // Autonomous touch without human presence is 100% ghost
                        severity = AnomalySeverity.CRITICAL,
                        category = TouchCategory.GHOST_MICRO_BURST,
                        description = "Phantom contact detected on untouched screen at (${ev.x.toInt()}, ${ev.y.toInt()})"
                    )
                    viewModel.recordAutonomousGhostTouch(anomaly)
                }
                false // Allow pass
            }
            .padding(16.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = TextPrimary)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = stringResource(R.string.diag_autonomous),
                        color = TextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Hands-Free Phantom Touch Detector",
                        color = NeonCyan,
                        fontSize = 11.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Instructions Box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(CardGlassNavy, RoundedCornerShape(12.dp))
                    .border(1.dp, GlassBorder, RoundedCornerShape(12.dp))
                    .padding(16.dp)
            ) {
                Column {
                    Text(
                        text = "TEST PROCEDURE:",
                        color = NeonCyan,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "1. Place phone flat on a table.\n2. Do NOT touch the screen during the timer.\n3. Any registered touches are hardware ghost spikes and will be logged below.",
                        color = TextSecondary,
                        fontSize = 11.sp,
                        lineHeight = 16.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Countdown / Status Timer
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = if (isRunning) "${countdown}s" else "READY",
                        color = if (isRunning) NeonCyan else TextMuted,
                        fontSize = 44.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text = if (isRunning) "LISTENING FOR PHANTOM CONTACTS..." else "Press Start and leave device still",
                        color = if (isRunning) NeonEmerald else TextSecondary,
                        fontSize = 11.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Start / Stop Button
            Button(
                onClick = {
                    if (isRunning) viewModel.stopAutonomousTest() else viewModel.startAutonomousTest(30)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isRunning) NeonDanger else Color(0xFF0D6EFD)
                )
            ) {
                Icon(if (isRunning) Icons.Default.Stop else Icons.Default.PlayArrow, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isRunning) "STOP TEST" else "START 30s AUTONOMOUS TEST",
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Results List
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "GHOST TOUCHES DETECTED: ${detectedList.size}",
                    color = if (detectedList.isNotEmpty()) NeonDanger else NeonEmerald,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(detectedList) { item ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF22111E), RoundedCornerShape(10.dp))
                            .border(1.dp, NeonDanger.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                            .padding(12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Dangerous, contentDescription = null, tint = NeonDanger)
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Phantom Contact at (${item.x.toInt()}px, ${item.y.toInt()}px)",
                                    color = TextPrimary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Time: +${item.timestamp % 100000}ms | Score: ${item.anomalyScore}/100",
                                    color = TextSecondary,
                                    fontSize = 10.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
