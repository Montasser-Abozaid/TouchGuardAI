package com.touchguard.ai.core.model

enum class ProfileType {
    NORMAL,
    SENSITIVE_SCREEN,
    DAMAGED_EDGE,
    DAMAGED_ZONE,
    DIAGNOSTIC,
    CUSTOM
}

data class ProtectionProfile(
    val type: ProfileType,
    val name: String,
    val description: String,
    val minTapDurationMs: Long = 12L,          // Impulses below this are considered ADC glitches
    val maxJitterRadiusPx: Float = 14f,        // Micro-cluster jitter threshold
    val maxVelocityPxPerMs: Float = 6.5f,      // Velocity beyond this considered teleportation/erratic jump
    val burstThresholdCount: Int = 4,          // Number of events in window triggering burst detection
    val burstWindowMs: Long = 90L,             // Time window for burst check
    val anomalyScoreThreshold: Int = 50,       // Score required to suppress event
    val enableDeadZoneOverlays: Boolean = true,
    val hapticWarningOnGhost: Boolean = false,
    val logAllEvents: Boolean = false
) {
    companion object {
        fun getPresets(): List<ProtectionProfile> = listOf(
            ProtectionProfile(
                type = ProfileType.NORMAL,
                name = "Normal Mode",
                description = "Balanced filtering for everyday use with mild screen blemishes",
                minTapDurationMs = 12L,
                maxJitterRadiusPx = 14f,
                maxVelocityPxPerMs = 6.5f,
                burstThresholdCount = 5,
                burstWindowMs = 90L,
                anomalyScoreThreshold = 55,
                enableDeadZoneOverlays = true
            ),
            ProtectionProfile(
                type = ProfileType.SENSITIVE_SCREEN,
                name = "Sensitive Screen Mode",
                description = "Strict jitter suppression for erratic digitizers with micro-cracks",
                minTapDurationMs = 20L,
                maxJitterRadiusPx = 22f,
                maxVelocityPxPerMs = 4.5f,
                burstThresholdCount = 3,
                burstWindowMs = 110L,
                anomalyScoreThreshold = 40,
                enableDeadZoneOverlays = true
            ),
            ProtectionProfile(
                type = ProfileType.DAMAGED_EDGE,
                name = "Damaged Edge Mode",
                description = "Reinforces lateral edge rejection to eliminate bezel touch bleeding",
                minTapDurationMs = 15L,
                maxJitterRadiusPx = 18f,
                maxVelocityPxPerMs = 5.0f,
                burstThresholdCount = 4,
                burstWindowMs = 100L,
                anomalyScoreThreshold = 45,
                enableDeadZoneOverlays = true
            ),
            ProtectionProfile(
                type = ProfileType.DAMAGED_ZONE,
                name = "Damaged Zone Mode",
                description = "Focuses defense on calibrated dead zone isolation layers",
                minTapDurationMs = 10L,
                maxJitterRadiusPx = 15f,
                maxVelocityPxPerMs = 7.0f,
                burstThresholdCount = 5,
                burstWindowMs = 80L,
                anomalyScoreThreshold = 60,
                enableDeadZoneOverlays = true
            ),
            ProtectionProfile(
                type = ProfileType.DIAGNOSTIC,
                name = "Diagnostic Mode",
                description = "Full telemetry logging mode: scores all events without dropping any",
                minTapDurationMs = 0L,
                maxJitterRadiusPx = 0f,
                maxVelocityPxPerMs = 999f,
                burstThresholdCount = 99,
                burstWindowMs = 10L,
                anomalyScoreThreshold = 101, // Never suppresses
                enableDeadZoneOverlays = false,
                logAllEvents = true
            )
        )
    }
}
