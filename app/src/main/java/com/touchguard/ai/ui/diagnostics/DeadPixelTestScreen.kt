package com.touchguard.ai.ui.diagnostics

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun DeadPixelTestScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = listOf(
        Color.White to "Pure White (Check Dark Sub-pixels)",
        Color.Black to "Pure Black (Check Stuck Bright Pixels)",
        Color.Red to "Pure Red (Check Green/Blue Sub-pixels)",
        Color.Green to "Pure Green (Check Red/Blue Sub-pixels)",
        Color.Blue to "Pure Blue (Check Yellow/Red Sub-pixels)",
        Color.Yellow to "Pure Yellow (Check Blue Sub-pixels)"
    )

    var colorIndex by remember { mutableIntStateOf(0) }
    val (currentColor, currentDesc) = colors[colorIndex]

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(currentColor)
            .clickable {
                if (colorIndex < colors.size - 1) {
                    colorIndex++
                } else {
                    onBack()
                }
            }
    ) {
        // Floating Hint Pill
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 32.dp)
                .background(Color(0x99000000), RoundedCornerShape(20.dp))
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "DEAD PIXEL TEST (${colorIndex + 1}/${colors.size}): $currentDesc",
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Tap screen to cycle color. Long press / last tap exits.",
                    color = Color.LightGray,
                    fontSize = 10.sp
                )
            }
        }
    }
}
