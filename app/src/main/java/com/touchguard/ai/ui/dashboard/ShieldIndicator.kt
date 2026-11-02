package com.touchguard.ai.ui.dashboard

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.touchguard.ai.ui.theme.ElectricBlue
import com.touchguard.ai.ui.theme.NeonCyan
import com.touchguard.ai.ui.theme.NeonDanger
import com.touchguard.ai.ui.theme.TextMuted
import com.touchguard.ai.ui.theme.TextPrimary

@Composable
fun AnimatedShieldIndicator(
    isActive: Boolean,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "shield_pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 0.7f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow_alpha"
    )

    Box(
        modifier = modifier.size(160.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(150.dp)) {
            val w = size.width
            val h = size.height

            // Outer Pulse Ring when active
            if (isActive) {
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(NeonCyan.copy(alpha = glowAlpha * 0.4f), Color.Transparent),
                        radius = (w / 2f) * pulseScale
                    )
                )
            }

            // Shield Path
            val shieldPath = Path().apply {
                moveTo(w * 0.5f, h * 0.12f)
                cubicTo(w * 0.75f, h * 0.12f, w * 0.88f, h * 0.25f, w * 0.88f, h * 0.45f)
                cubicTo(w * 0.88f, h * 0.75f, w * 0.5f, h * 0.92f, w * 0.5f, h * 0.92f)
                cubicTo(w * 0.5f, h * 0.92f, w * 0.12f, h * 0.75f, w * 0.12f, h * 0.45f)
                cubicTo(w * 0.12f, h * 0.25f, w * 0.25f, h * 0.12f, w * 0.5f, h * 0.12f)
                close()
            }

            // Fill Shield
            val fillBrush = if (isActive) {
                Brush.verticalGradient(
                    colors = listOf(Color(0xFF0F2650), Color(0xFF071228))
                )
            } else {
                Brush.verticalGradient(
                    colors = listOf(Color(0xFF141A29), Color(0xFF090D15))
                )
            }
            drawPath(path = shieldPath, brush = fillBrush)

            // Shield Stroke Border
            val strokeColor = if (isActive) NeonCyan else Color(0xFF334155)
            drawPath(
                path = shieldPath,
                color = strokeColor,
                style = Stroke(width = if (isActive) 3.dp.toPx() else 1.5.dp.toPx())
            )
        }

        Text(
            text = if (isActive) "ACTIVE" else "STANDBY",
            color = if (isActive) TextPrimary else TextMuted,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.5.sp
        )
    }
}
