package com.touchguard.ai.core.model

/**
 * A scored, user-facing record of a detected ghost-touch anomaly, emitted by
 * [com.touchguard.ai.engine.TouchAnalysisEngine] whenever an event's anomaly
 * score crosses the reporting threshold. Persisted (as [com.touchguard.ai.data.local.IncidentLogEntity])
 * for the incident history and dashboard telemetry.
 */
data class GhostTouchAnomaly(
    val timestamp: Long,
    val x: Float,
    val y: Float,
    val anomalyScore: Int,
    val severity: AnomalySeverity,
    val category: TouchCategory,
    val description: String,
    val burstCount: Int = 1,
    val deltaDurationMs: Long = 0L,
    val matchedDeadZoneId: Long? = null
)
