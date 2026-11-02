package com.touchguard.ai.ui.dashboard

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Dangerous
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.touchguard.ai.R
import com.touchguard.ai.core.model.AnomalySeverity
import com.touchguard.ai.ui.navigation.Screen
import com.touchguard.ai.ui.theme.CardGlassNavy
import com.touchguard.ai.ui.theme.DarkNavyBackground
import com.touchguard.ai.ui.theme.ElectricBlue
import com.touchguard.ai.ui.theme.GlassBorder
import com.touchguard.ai.ui.theme.GlassCard
import com.touchguard.ai.ui.theme.NeonAmber
import com.touchguard.ai.ui.theme.NeonCyan
import com.touchguard.ai.ui.theme.NeonDanger
import com.touchguard.ai.ui.theme.NeonEmerald
import com.touchguard.ai.ui.theme.TextMuted
import com.touchguard.ai.ui.theme.TextPrimary
import com.touchguard.ai.ui.theme.TextSecondary
import com.touchguard.ai.viewmodel.MainViewModel

@Composable
fun DashboardScreen(
    viewModel: MainViewModel,
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isShieldActive by viewModel.isShieldActive.collectAsState()
    val threatLevel by viewModel.threatLevel.collectAsState()
    val totalTouches by viewModel.totalTouchEvents.collectAsState()
    val filteredEvents by viewModel.filteredGhostEvents.collectAsState()
    val deadZones by viewModel.deadZones.collectAsState()
    val activityHistory by viewModel.activityHistory.collectAsState()
    val deviceProfile by viewModel.deviceProfile.collectAsState()
    val currentProfile by viewModel.currentProfile.collectAsState()

    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkNavyBackground)
            .verticalScroll(scrollState)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // App Header & Branding
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = stringResource(R.string.app_name),
                    color = TextPrimary,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.sp
                )
                Text(
                    text = stringResource(R.string.app_tagline),
                    color = NeonCyan,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            // Honor 8X Hardware Badge
            Box(
                modifier = Modifier
                    .background(
                        if (deviceProfile.isHonor8XSeries) Color(0xFF14274E) else CardGlassNavy,
                        RoundedCornerShape(8.dp)
                    )
                    .border(
                        1.dp,
                        if (deviceProfile.isHonor8XSeries) NeonCyan else GlassBorder,
                        RoundedCornerShape(8.dp)
                    )
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = if (deviceProfile.isHonor8XSeries) "HONOR 8X OPTIMIZED" else deviceProfile.model,
                    color = if (deviceProfile.isHonor8XSeries) NeonCyan else TextSecondary,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Center Animated Shield
        AnimatedShieldIndicator(isActive = isShieldActive)

        Spacer(modifier = Modifier.height(16.dp))

        // Threat Level Ribbon
        val (threatText, threatColor) = when (threatLevel) {
            AnomalySeverity.LOW -> stringResource(R.string.metric_threat_low) to NeonEmerald
            AnomalySeverity.MEDIUM -> stringResource(R.string.metric_threat_medium) to NeonAmber
            AnomalySeverity.HIGH -> stringResource(R.string.metric_threat_high) to NeonDanger
            AnomalySeverity.CRITICAL -> stringResource(R.string.metric_threat_critical) to Color(0xFFFF0055)
        }

        Row(
            modifier = Modifier
                .background(threatColor.copy(alpha = 0.15f), RoundedCornerShape(20.dp))
                .border(1.dp, threatColor.copy(alpha = 0.4f), RoundedCornerShape(20.dp))
                .padding(horizontal = 14.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(threatColor, CircleShape)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "${stringResource(R.string.metric_threat_level)}: $threatText",
                color = threatColor,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Big Primary Toggle Button (ENGAGE / DISENGAGE)
        Button(
            onClick = {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(context)) {
                    val intent = Intent(
                        Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:${context.packageName}")
                    )
                    context.startActivity(intent)
                } else {
                    viewModel.toggleShield(context)
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isShieldActive) NeonDanger else ElectricBlue
            )
        ) {
            Icon(
                imageVector = if (isShieldActive) Icons.Default.Stop else Icons.Default.PlayArrow,
                contentDescription = null,
                tint = Color.White
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = if (isShieldActive) stringResource(R.string.shield_toggle_stop) else stringResource(R.string.shield_toggle_start),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Emergency Killswitch
        OutlinedButton(
            onClick = { viewModel.emergencyKillswitch(context) },
            modifier = Modifier
                .fillMaxWidth()
                .height(44.dp),
            shape = RoundedCornerShape(12.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, NeonDanger.copy(alpha = 0.7f)),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = NeonDanger
            )
        ) {
            Icon(Icons.Default.Dangerous, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = stringResource(R.string.emergency_disable),
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Telemetry Statistics Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Blocked Ghost Events
            GlassCard(modifier = Modifier.weight(1f)) {
                Column {
                    Text(
                        text = stringResource(R.string.metric_abnormal_events),
                        color = TextSecondary,
                        fontSize = 11.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = filteredEvents.toString(),
                        color = NeonCyan,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Active Dead Zones
            val activeZonesCount = deadZones.count { it.isEnabled }
            GlassCard(modifier = Modifier.weight(1f)) {
                Column {
                    Text(
                        text = stringResource(R.string.metric_active_deadzones),
                        color = TextSecondary,
                        fontSize = 11.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = activeZonesCount.toString(),
                        color = if (activeZonesCount > 0) NeonAmber else TextPrimary,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Live Touch Frequency Graph Card
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.activity_graph_title),
                        color = TextSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "${totalTouches} events",
                        color = NeonCyan,
                        fontSize = 10.sp
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                ActivityGraph(history = activityHistory)
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Navigation Action Grid
        Text(
            text = "COMMAND & CONTROL",
            color = TextMuted,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.fillMaxWidth(),
            letterSpacing = 1.sp
        )

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Diagnostics Suite
            ActionCard(
                title = stringResource(R.string.nav_diagnostics),
                subtitle = "Screen Grid & Tests",
                icon = Icons.Default.Build,
                accentColor = NeonCyan,
                modifier = Modifier.weight(1f),
                onClick = { onNavigate(Screen.Diagnostics.route) }
            )

            // Dead Zones
            ActionCard(
                title = stringResource(R.string.nav_deadzones),
                subtitle = "Shield Broken Areas",
                icon = Icons.Default.Layers,
                accentColor = NeonDanger,
                modifier = Modifier.weight(1f),
                onClick = { onNavigate(Screen.DeadZones.route) }
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Smart Profiles
            ActionCard(
                title = stringResource(R.string.nav_profiles),
                subtitle = currentProfile.name,
                icon = Icons.Default.Tune,
                accentColor = NeonAmber,
                modifier = Modifier.weight(1f),
                onClick = { onNavigate(Screen.Profiles.route) }
            )

            // Ghost Touch Simulator
            ActionCard(
                title = stringResource(R.string.nav_simulator),
                subtitle = "Validate Algorithm",
                icon = Icons.Default.Science,
                accentColor = NeonEmerald,
                modifier = Modifier.weight(1f),
                onClick = { onNavigate(Screen.Simulator.route) }
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // System Research Button
        ActionCard(
            title = stringResource(R.string.nav_reports),
            subtitle = "AOSP Architecture, Honor 8X Digitizer & Feasibility Analysis",
            icon = Icons.Default.Security,
            accentColor = ElectricBlue,
            modifier = Modifier.fillMaxWidth(),
            onClick = { onNavigate(Screen.Reports.route) }
        )

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun ActionCard(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    accentColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.clickable { onClick() },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = CardGlassNavy),
        border = androidx.compose.foundation.BorderStroke(1.dp, GlassBorder)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(accentColor.copy(alpha = 0.15f), RoundedCornerShape(10.dp))
                    .border(1.dp, accentColor.copy(alpha = 0.3f), RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = accentColor, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(text = title, color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                Text(text = subtitle, color = TextSecondary, fontSize = 10.sp, maxLines = 1)
            }
        }
    }
}
