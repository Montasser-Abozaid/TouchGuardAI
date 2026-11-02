package com.touchguard.ai.core.model

/**
 * Severity bucket for a detected ghost-touch anomaly, driven by the computed anomaly score.
 */
enum class AnomalySeverity {
    LOW,
    MEDIUM,
    HIGH,
    CRITICAL
}

/**
 * Classification of a touch event based on the spatial/temporal/physical heuristics
 * evaluated by [com.touchguard.ai.engine.TouchAnalysisEngine].
 */
enum class TouchCategory {
    UNKNOWN,
    LEGITIMATE_TAP,
    LEGITIMATE_DRAG,
    GHOST_DEADZONE_INTRUSION,
    GHOST_ERRATIC_JUMP,
    GHOST_STATIC_JITTER,
    GHOST_MICRO_BURST,
    GHOST_UNNATURAL_MULTITOUCH
}

/**
 * Normalized snapshot of a single Android [android.view.MotionEvent] pointer sample,
 * decoupled from the framework type so it can be replayed, simulated and unit tested.
 */
data class RawMotionEvent(
    val action: Int,
    val actionMasked: Int,
    val pointerId: Int,
    val x: Float,
    val y: Float,
    val pressure: Float,
    val size: Float,
    val eventTime: Long,
    val downTime: Long,
    val pointerCount: Int = 1
) {
    /** Elapsed time since the pointer first went down, in milliseconds. */
    val durationMs: Long
        get() = (eventTime - downTime).coerceAtLeast(0)
}

/**
 * Result of running a [RawMotionEvent] through the ghost-touch analysis engine:
 * the original sample plus the computed score, classification and whether it was suppressed.
 */
data class FilteredTouchEvent(
    val raw: RawMotionEvent,
    val anomalyScore: Int,
    val isSuppressed: Boolean,
    val suppressionReason: String?,
    val category: TouchCategory,
    val calculatedVelocity: Float = 0f,
    val jitterRadius: Float = 0f
)
