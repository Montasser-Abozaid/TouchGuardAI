package com.touchguard.ai.core.model

enum class DeadZoneMode {
    MONITOR_ONLY,
    BLOCK_TOUCH
}

data class DeadZoneConfig(
    val id: Long = 0,
    val name: String,
    val leftRatio: Float,   // 0.0 to 1.0 (relative to screen width)
    val topRatio: Float,    // 0.0 to 1.0 (relative to screen height)
    val rightRatio: Float,  // 0.0 to 1.0
    val bottomRatio: Float, // 0.0 to 1.0
    val mode: DeadZoneMode = DeadZoneMode.BLOCK_TOUCH,
    val isEnabled: Boolean = true,
    val incidentCount: Int = 0,
    val colorHex: String = "#FF1744"
) {
    fun contains(x: Float, y: Float, screenWidth: Int, screenHeight: Int): Boolean {
        val left = leftRatio * screenWidth
        val top = topRatio * screenHeight
        val right = rightRatio * screenWidth
        val bottom = bottomRatio * screenHeight
        return x in left..right && y in top..bottom
    }

    companion object {
        fun createTopEdgePreset() = DeadZoneConfig(
            id = 1,
            name = "Top Edge & Notch",
            leftRatio = 0.0f,
            topRatio = 0.0f,
            rightRatio = 1.0f,
            bottomRatio = 0.06f,
            mode = DeadZoneMode.BLOCK_TOUCH
        )

        fun createRightEdgePreset() = DeadZoneConfig(
            id = 2,
            name = "Right Lateral Edge",
            leftRatio = 0.94f,
            topRatio = 0.0f,
            rightRatio = 1.0f,
            bottomRatio = 1.0f,
            mode = DeadZoneMode.BLOCK_TOUCH
        )

        fun createLeftEdgePreset() = DeadZoneConfig(
            id = 3,
            name = "Left Lateral Edge",
            leftRatio = 0.0f,
            topRatio = 0.0f,
            rightRatio = 0.06f,
            bottomRatio = 1.0f,
            mode = DeadZoneMode.BLOCK_TOUCH
        )

        fun createCenterCrackPreset() = DeadZoneConfig(
            id = 4,
            name = "Center Crack Zone",
            leftRatio = 0.25f,
            topRatio = 0.35f,
            rightRatio = 0.75f,
            bottomRatio = 0.55f,
            mode = DeadZoneMode.BLOCK_TOUCH
        )
    }
}
