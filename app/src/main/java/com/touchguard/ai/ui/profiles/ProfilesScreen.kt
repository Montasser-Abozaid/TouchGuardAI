package com.touchguard.ai.ui.profiles

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
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
import com.touchguard.ai.core.model.ProtectionProfile
import com.touchguard.ai.ui.theme.CardGlassNavy
import com.touchguard.ai.ui.theme.DarkNavyBackground
import com.touchguard.ai.ui.theme.ElectricBlue
import com.touchguard.ai.ui.theme.GlassBorder
import com.touchguard.ai.ui.theme.NeonAmber
import com.touchguard.ai.ui.theme.NeonCyan
import com.touchguard.ai.ui.theme.NeonEmerald
import com.touchguard.ai.ui.theme.TextMuted
import com.touchguard.ai.ui.theme.TextPrimary
import com.touchguard.ai.ui.theme.TextSecondary
import com.touchguard.ai.viewmodel.MainViewModel

@Composable
fun ProfilesScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val currentProfile by viewModel.currentProfile.collectAsState()
    val presets = ProtectionProfile.getPresets()

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
                    text = stringResource(R.string.profiles_title),
                    color = TextPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Adaptive Filtering Sensitivity Presets",
                    color = NeonCyan,
                    fontSize = 11.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(presets) { profile ->
                val isSelected = (profile.type == currentProfile.type)

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { viewModel.setProfile(profile) },
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) Color(0xFF14244A) else CardGlassNavy
                    ),
                    border = androidx.compose.foundation.BorderStroke(
                        if (isSelected) 2.dp else 1.dp,
                        if (isSelected) NeonCyan else GlassBorder
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = profile.name,
                                color = if (isSelected) NeonCyan else TextPrimary,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )

                            if (isSelected) {
                                Icon(
                                    Icons.Default.CheckCircle,
                                    contentDescription = "Selected",
                                    tint = NeonCyan,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = profile.description,
                            color = TextSecondary,
                            fontSize = 11.sp,
                            lineHeight = 15.sp
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Parameter Badges
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            ParamBadge(label = "Min Tap", value = "${profile.minTapDurationMs}ms")
                            ParamBadge(label = "Jitter", value = "${profile.maxJitterRadiusPx.toInt()}px")
                            ParamBadge(label = "Anomaly Cutoff", value = "${profile.anomalyScoreThreshold}%")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ParamBadge(label: String, value: String) {
    Box(
        modifier = Modifier
            .background(Color(0xFF091224), RoundedCornerShape(6.dp))
            .border(1.dp, Color(0xFF1E2D4A), RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = "$label: ", color = TextMuted, fontSize = 9.sp)
            Text(text = value, color = NeonEmerald, fontSize = 9.sp, fontWeight = FontWeight.Bold)
        }
    }
}
