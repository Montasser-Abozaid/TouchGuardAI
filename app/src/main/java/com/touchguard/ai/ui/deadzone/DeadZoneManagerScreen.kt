package com.touchguard.ai.ui.deadzone

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.CropFree
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import com.touchguard.ai.core.model.DeadZoneConfig
import com.touchguard.ai.core.model.DeadZoneMode
import com.touchguard.ai.data.local.DeadZoneEntity
import com.touchguard.ai.ui.navigation.Screen
import com.touchguard.ai.ui.theme.CardGlassNavy
import com.touchguard.ai.ui.theme.DarkNavyBackground
import com.touchguard.ai.ui.theme.ElectricBlue
import com.touchguard.ai.ui.theme.GlassBorder
import com.touchguard.ai.ui.theme.NeonAmber
import com.touchguard.ai.ui.theme.NeonCyan
import com.touchguard.ai.ui.theme.NeonDanger
import com.touchguard.ai.ui.theme.NeonEmerald
import com.touchguard.ai.ui.theme.TextMuted
import com.touchguard.ai.ui.theme.TextPrimary
import com.touchguard.ai.ui.theme.TextSecondary
import com.touchguard.ai.viewmodel.DeadZoneViewModel

@Composable
fun DeadZoneManagerScreen(
    viewModel: DeadZoneViewModel,
    onNavigate: (String) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val deadZones by viewModel.deadZones.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkNavyBackground)
            .padding(16.dp)
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
                    text = stringResource(R.string.deadzone_title),
                    color = TextPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Shield physically damaged digitizer zones",
                    color = NeonCyan,
                    fontSize = 11.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Technical Warning Banner (Mandatory requirement #4)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF2E1A1A), RoundedCornerShape(12.dp))
                .border(1.dp, NeonDanger.copy(alpha = 0.6f), RoundedCornerShape(12.dp))
                .padding(12.dp)
        ) {
            Row(verticalAlignment = Alignment.Top) {
                Icon(Icons.Default.Warning, contentDescription = null, tint = NeonDanger, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = stringResource(R.string.deadzone_warning),
                    color = Color(0xFFFFCDD2),
                    fontSize = 10.sp,
                    lineHeight = 14.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Visual Calibration Action Button
        Button(
            onClick = { onNavigate(Screen.InteractiveCalibration.route) },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue)
        ) {
            Icon(Icons.Default.CropFree, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = stringResource(R.string.deadzone_calibrate),
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Presets Row
        Text(
            text = "QUICK PRESETS (HONOR 8X)",
            color = TextMuted,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )

        Spacer(modifier = Modifier.height(6.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(
                onClick = { viewModel.addPreset(DeadZoneConfig.createTopEdgePreset()) },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(8.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, GlassBorder)
            ) {
                Text("+ Top Edge", color = NeonCyan, fontSize = 10.sp)
            }
            OutlinedButton(
                onClick = { viewModel.addPreset(DeadZoneConfig.createRightEdgePreset()) },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(8.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, GlassBorder)
            ) {
                Text("+ Right Edge", color = NeonCyan, fontSize = 10.sp)
            }
            OutlinedButton(
                onClick = { viewModel.addPreset(DeadZoneConfig.createCenterCrackPreset()) },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(8.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, GlassBorder)
            ) {
                Text("+ Center", color = NeonCyan, fontSize = 10.sp)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Dead Zones List
        Text(
            text = "CONFIGURED DEAD ZONES (${deadZones.size})",
            color = TextMuted,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )

        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(deadZones) { zone ->
                DeadZoneItemCard(
                    zone = zone,
                    onToggleEnabled = { viewModel.toggleZoneEnabled(zone) },
                    onToggleMode = { viewModel.toggleZoneMode(zone) },
                    onDelete = { viewModel.deleteZone(zone.id) }
                )
            }
        }
    }
}

@Composable
fun DeadZoneItemCard(
    zone: DeadZoneEntity,
    onToggleEnabled: () -> Unit,
    onToggleMode: () -> Unit,
    onDelete: () -> Unit
) {
    val isBlocking = zone.mode == DeadZoneMode.BLOCK_TOUCH.name

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = CardGlassNavy),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (zone.isEnabled) NeonDanger.copy(alpha = 0.5f) else GlassBorder
        )
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .background(if (zone.isEnabled) NeonDanger else TextMuted, RoundedCornerShape(2.dp))
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = zone.name,
                        color = TextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Switch(
                    checked = zone.isEnabled,
                    onCheckedChange = { onToggleEnabled() },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = NeonCyan,
                        checkedTrackColor = ElectricBlue
                    )
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Coordinates Bounds
            Text(
                text = "Coverage: [L: ${(zone.leftRatio * 100).toInt()}% .. R: ${(zone.rightRatio * 100).toInt()}%] x [T: ${(zone.topRatio * 100).toInt()}% .. B: ${(zone.bottomRatio * 100).toInt()}%]",
                color = TextSecondary,
                fontSize = 10.sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Mode Toggle & Delete
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = onToggleMode,
                    shape = RoundedCornerShape(6.dp),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (isBlocking) NeonDanger else NeonCyan
                    )
                ) {
                    Icon(
                        if (isBlocking) Icons.Default.Block else Icons.Default.Visibility,
                        contentDescription = null,
                        tint = if (isBlocking) NeonDanger else NeonCyan,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isBlocking) "BLOCK TOUCH" else "MONITOR ONLY",
                        color = if (isBlocking) NeonDanger else NeonCyan,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Gray, modifier = Modifier.size(20.dp))
                }
            }
        }
    }
}
