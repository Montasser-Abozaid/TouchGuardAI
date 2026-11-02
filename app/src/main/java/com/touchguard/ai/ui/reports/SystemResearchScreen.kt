package com.touchguard.ai.ui.reports

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Dangerous
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.touchguard.ai.R
import com.touchguard.ai.ui.theme.CardGlassNavy
import com.touchguard.ai.ui.theme.DarkNavyBackground
import com.touchguard.ai.ui.theme.GlassBorder
import com.touchguard.ai.ui.theme.NeonAmber
import com.touchguard.ai.ui.theme.NeonCyan
import com.touchguard.ai.ui.theme.NeonDanger
import com.touchguard.ai.ui.theme.NeonEmerald
import com.touchguard.ai.ui.theme.TextMuted
import com.touchguard.ai.ui.theme.TextPrimary
import com.touchguard.ai.ui.theme.TextSecondary
import com.touchguard.ai.viewmodel.MainViewModel

@Composable
fun SystemResearchScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val deviceProfile by viewModel.deviceProfile.collectAsState()
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkNavyBackground)
            .padding(16.dp)
            .verticalScroll(scrollState)
    ) {
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
                    text = "AOSP System Research & Feasibility",
                    color = TextPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Android Input Pipeline & Honor 8X Technical Assessment",
                    color = NeonCyan,
                    fontSize = 11.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Device Profile Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = CardGlassNavy),
            border = androidx.compose.foundation.BorderStroke(1.dp, NeonCyan.copy(alpha = 0.4f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "PROFILED HARDWARE & SUBSYSTEM",
                    color = NeonCyan,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                ProfileRow("Model", "${deviceProfile.model} (${deviceProfile.deviceCode})")
                ProfileRow("SoC", "${deviceProfile.hardwareBoard} (Kirin 710: ${deviceProfile.socKirin710Detected})")
                ProfileRow("EMUI", deviceProfile.emuiVersion)
                ProfileRow("OS", "Android ${deviceProfile.androidVersion} (API ${deviceProfile.apiLevel})")
                ProfileRow("Screen", "${deviceProfile.screenWidthPx}x${deviceProfile.screenHeightPx} @ ${deviceProfile.refreshRateHz}Hz")
                ProfileRow("Driver", deviceProfile.touchDriverSummary)
                ProfileRow("/dev/input", deviceProfile.devInputNodesFound.joinToString(", "))
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "THREE-TIER PROTECTION REALITY MATRIX",
            color = TextMuted,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Level 1 Card
        LevelStatusCard(
            level = "LEVEL 1: APP-LEVEL PROTECTION",
            status = "IMPLEMENTED",
            statusColor = NeonEmerald,
            summary = "Custom ViewGroup & DecorView touch interception, stateful anomaly scoring, and phantom dropping inside Touch Guard UI.",
            techDetails = "Uses dispatchTouchEvent and onInterceptTouchEvent with circular pointer buffers. 100% functional on standard unrooted devices without special privileges."
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Level 2 Card
        LevelStatusCard(
            level = "LEVEL 2: ACCESSIBILITY & OVERLAY SHIELDS",
            status = "PARTIALLY IMPLEMENTED (DESIGN BOUNDED)",
            statusColor = NeonAmber,
            summary = "Smart Dead Zone blocking overlays using TYPE_APPLICATION_OVERLAY / TYPE_ACCESSIBILITY_OVERLAY with FLAG_NOT_TOUCH_MODAL.",
            techDetails = "BLOCKING WORKS: Creating an overlay over a damaged strip (e.g. notch, edge) completely blocks ghost touches from reaching background apps. SELECTIVE FORWARDING NOT POSSIBLE: Android security model intentionally prevents untrusted apps from selectively filtering and passing touches to lower windows without consuming or injecting."
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Level 3 Card
        LevelStatusCard(
            level = "LEVEL 3: SYSTEM-LEVEL INPUT FILTERING",
            status = "NOT POSSIBLE ON STOCK HONOR 8X (ROOT / SIGNATURE REQUIRED)",
            statusColor = NeonDanger,
            summary = "AOSP InputManager.setInputFilter requires android.permission.INJECT_EVENTS (signature-level).",
            techDetails = "Stock Huawei Honor 8X operates on locked bootloader (Kirin 710). Huawei terminated unlock codes in July 2018. Installing an AOSP InputFilter before InputDispatcher delivers events to apps requires either: (A) System ROM Signature, (B) Magisk / KernelSU root, or (C) Custom ROM. Documented architecture is provided for rooted test environments."
        )

        Spacer(modifier = Modifier.height(20.dp))
    }
}

@Composable
fun ProfileRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = "$label:", color = TextSecondary, fontSize = 11.sp)
        Text(text = value, color = TextPrimary, fontSize = 11.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun LevelStatusCard(
    level: String,
    status: String,
    statusColor: Color,
    summary: String,
    techDetails: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = CardGlassNavy),
        border = androidx.compose.foundation.BorderStroke(1.dp, GlassBorder)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = level, color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Box(
                    modifier = Modifier
                        .background(statusColor.copy(alpha = 0.15f), RoundedCornerShape(6.dp))
                        .border(1.dp, statusColor.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(text = status, color = statusColor, fontSize = 9.sp, fontWeight = FontWeight.ExtraBold)
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(text = summary, color = TextSecondary, fontSize = 11.sp, lineHeight = 15.sp)

            Spacer(modifier = Modifier.height(8.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF070B19), RoundedCornerShape(8.dp))
                    .padding(10.dp)
            ) {
                Text(text = techDetails, color = Color(0xFF90A4AE), fontSize = 10.sp, lineHeight = 14.sp)
            }
        }
    }
}
