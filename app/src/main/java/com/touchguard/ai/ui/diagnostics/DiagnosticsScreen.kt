package com.touchguard.ai.ui.diagnostics

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.FiberManualRecord
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.LinearScale
import androidx.compose.material.icons.filled.PanTool
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.touchguard.ai.R
import com.touchguard.ai.ui.navigation.Screen
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

@Composable
fun DiagnosticsScreen(
    onNavigate: (String) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkNavyBackground)
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // Header
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = TextPrimary
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = stringResource(R.string.diag_title),
                    color = TextPrimary,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Hardware Digitizer & Screen Matrix Analyzer",
                    color = NeonCyan,
                    fontSize = 11.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Diagnostic Options
        DiagnosticOptionCard(
            title = stringResource(R.string.diag_grid_test),
            description = stringResource(R.string.diag_grid_desc),
            icon = Icons.Default.GridOn,
            accentColor = NeonCyan,
            onClick = { onNavigate(Screen.TouchGridTest.route) }
        )

        Spacer(modifier = Modifier.height(12.dp))

        DiagnosticOptionCard(
            title = stringResource(R.string.diag_multitouch),
            description = stringResource(R.string.diag_multitouch_desc),
            icon = Icons.Default.PanTool,
            accentColor = NeonEmerald,
            onClick = { onNavigate(Screen.MultiTouchTest.route) }
        )

        Spacer(modifier = Modifier.height(12.dp))

        DiagnosticOptionCard(
            title = stringResource(R.string.diag_autonomous),
            description = stringResource(R.string.diag_autonomous_desc),
            icon = Icons.Default.Timer,
            accentColor = NeonDanger,
            onClick = { onNavigate(Screen.AutonomousTest.route) }
        )

        Spacer(modifier = Modifier.height(12.dp))

        DiagnosticOptionCard(
            title = stringResource(R.string.diag_linear_test),
            description = stringResource(R.string.diag_linear_desc),
            icon = Icons.Default.LinearScale,
            accentColor = Color(0xFF7C4DFF),
            onClick = { onNavigate(Screen.LinearityTest.route) }
        )

        Spacer(modifier = Modifier.height(12.dp))

        DiagnosticOptionCard(
            title = stringResource(R.string.diag_deadpixel),
            description = stringResource(R.string.diag_deadpixel_desc),
            icon = Icons.Default.FiberManualRecord,
            accentColor = NeonAmber,
            onClick = { onNavigate(Screen.DeadPixelTest.route) }
        )

        Spacer(modifier = Modifier.height(12.dp))

        DiagnosticOptionCard(
            title = stringResource(R.string.diag_heatmap),
            description = stringResource(R.string.diag_heatmap_desc),
            icon = Icons.Default.Whatshot,
            accentColor = Color(0xFFFF5252),
            onClick = { onNavigate(Screen.HeatmapTest.route) }
        )
    }
}

@Composable
fun DiagnosticOptionCard(
    title: String,
    description: String,
    icon: ImageVector,
    accentColor: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = CardGlassNavy),
        border = androidx.compose.foundation.BorderStroke(1.dp, GlassBorder)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(accentColor.copy(alpha = 0.15f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = accentColor, modifier = Modifier.size(24.dp))
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = description, color = TextSecondary, fontSize = 11.sp, lineHeight = 15.sp)
            }
        }
    }
}
