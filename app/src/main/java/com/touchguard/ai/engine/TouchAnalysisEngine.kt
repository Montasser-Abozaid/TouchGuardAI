package com.touchguard.ai.engine

import android.view.MotionEvent
import com.touchguard.ai.core.model.AnomalySeverity
import com.touchguard.ai.core.model.DeadZoneConfig
import com.touchguard.ai.core.model.DeadZoneMode
import com.touchguard.ai.core.model.FilteredTouchEvent
import com.touchguard.ai.core.model.GhostTouchAnomaly
import com.touchguard.ai.core.model.ProfileType
import com.touchguard.ai.core.model.ProtectionProfile
import com.touchguard.ai.core.model.RawMotionEvent
import com.touchguard.ai.core.model.TouchCategory
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.hypot
import kotlin.math.sqrt

class TouchAnalysisEngine(
    private var profile: ProtectionProfile = ProtectionProfile.getPresets()[0],
    private var screenWidth: Int = 1080,
    private var screenHeight: Int = 2340
) {
    // Recent history for pointer trajectories
    private val pointerHistories = mutableMapOf<Int, MutableList<RawMotionEvent>>()
    // Recent tap events across all pointers for burst frequency detection
    private val recentDownEvents = ArrayDeque<RawMotionEvent>()
    
    // Active dead zones
    private val deadZones = mutableListOf<DeadZoneConfig>()

    // Telemetry flows
    private val _anomalyFlow = MutableSharedFlow<GhostTouchAnomaly>(extraBufferCapacity = 64)
    val anomalyFlow: SharedFlow<GhostTouchAnomaly> = _anomalyFlow.asSharedFlow()

    private val _recentEventsFlow = MutableSharedFlow<FilteredTouchEvent>(extraBufferCapacity = 128)
    val recentEventsFlow: SharedFlow<FilteredTouchEvent> = _recentEventsFlow.asSharedFlow()

    private val _totalEventsCount = MutableStateFlow(0L)
    val totalEventsCount: StateFlow<Long> = _totalEventsCount.asStateFlow()

    private val _suppressedEventsCount = MutableStateFlow(0L)
    val suppressedEventsCount: StateFlow<Long> = _suppressedEventsCount.asStateFlow()

    private val _currentThreatLevel = MutableStateFlow(AnomalySeverity.LOW)
    val currentThreatLevel: StateFlow<AnomalySeverity> = _currentThreatLevel.asStateFlow()

    fun updateScreenDimensions(width: Int, height: Int) {
        screenWidth = width.coerceAtLeast(1)
        screenHeight = height.coerceAtLeast(1)
    }

    fun updateProfile(newProfile: ProtectionProfile) {
        profile = newProfile
    }

    fun updateDeadZones(zones: List<DeadZoneConfig>) {
        synchronized(deadZones) {
            deadZones.clear()
            deadZones.addAll(zones.filter { it.isEnabled })
        }
    }

    /**
     * Ingest an Android MotionEvent, process all pointers, and determine filtering.
     * Returns true if event should be consumed / dropped, false if it can proceed.
     */
    fun processMotionEvent(event: MotionEvent): Boolean {
        val actionMasked = event.actionMasked
        val pointerIndex = event.actionIndex
        val pointerId = event.getPointerId(pointerIndex)
        val x = event.getX(pointerIndex)
        val y = event.getY(pointerIndex)
        val pressure = event.getPressure(pointerIndex)
        val size = event.getSize(pointerIndex)
        val eventTime = event.eventTime
        val downTime = event.downTime

        val raw = RawMotionEvent(
            action = event.action,
            actionMasked = actionMasked,
            pointerId = pointerId,
            x = x,
            y = y,
            pressure = pressure,
            size = size,
            eventTime = eventTime,
            downTime = downTime,
            pointerCount = event.pointerCount
        )

        val filtered = analyzeEvent(raw)
        _totalEventsCount.value += 1
        if (filtered.isSuppressed) {
            _suppressedEventsCount.value += 1
        }
        _recentEventsFlow.tryEmit(filtered)

        return filtered.isSuppressed
    }

    /**
     * Core Algorithm: Analyzes spatial, temporal, and physical dynamics
     */
    fun analyzeEvent(raw: RawMotionEvent): FilteredTouchEvent {
        var score = 0
        var category = TouchCategory.UNKNOWN
        val suppressionReasons = mutableListOf<String>()
        var calculatedVelocity = 0f
        var jitterRadius = 0f

        // 1. Check Dead Zone Boundaries
        val matchedDeadZone = findMatchingDeadZone(raw.x, raw.y)
        if (matchedDeadZone != null) {
            category = TouchCategory.GHOST_DEADZONE_INTRUSION
            if (matchedDeadZone.mode == DeadZoneMode.BLOCK_TOUCH) {
                score += 85
                suppressionReasons.add("Dead Zone block (${matchedDeadZone.name})")
            } else {
                score += 40
                suppressionReasons.add("Dead Zone monitor (${matchedDeadZone.name})")
            }
        }

        // 2. Multi-pointer history tracking
        val history = pointerHistories.getOrPut(raw.pointerId) { mutableListOf() }
        val prevEvent = history.lastOrNull()

        if (prevEvent != null) {
            val deltaDist = hypot(raw.x - prevEvent.x, raw.y - prevEvent.y)
            val deltaT = (raw.eventTime - prevEvent.eventTime).coerceAtLeast(1)
            calculatedVelocity = deltaDist / deltaT
            jitterRadius = deltaDist

            // Teleportation / Impossible Velocity Check
            if (calculatedVelocity > profile.maxVelocityPxPerMs && deltaDist > 80f) {
                score += 55
                category = TouchCategory.GHOST_ERRATIC_JUMP
                suppressionReasons.add("Erratic jump velocity (${String.format("%.1f", calculatedVelocity)} px/ms)")
            }

            // Micro-Tremor Stationary Jitter in tight radius with rapid oscillations
            if (raw.actionMasked == MotionEvent.ACTION_MOVE && deltaDist < profile.maxJitterRadiusPx && deltaT < 16) {
                // Check if last 3 events also stayed in this sub-pixel envelope
                if (history.size >= 3) {
                    val recent3 = history.takeLast(3)
                    val maxSpread = recent3.maxOf { hypot(it.x - raw.x, it.y - raw.y) }
                    if (maxSpread < profile.maxJitterRadiusPx) {
                        score += 35
                        category = TouchCategory.GHOST_STATIC_JITTER
                        suppressionReasons.add("Stationary digitizer micro-tremor")
                    }
                }
            }
        }

        // 3. Sub-Frame ADC Glitch (Ultra-Short Impulses)
        if (raw.actionMasked == MotionEvent.ACTION_UP) {
            if (raw.durationMs < profile.minTapDurationMs) {
                score += 50
                category = TouchCategory.GHOST_MICRO_BURST
                suppressionReasons.add("Sub-frame ADC pulse (${raw.durationMs}ms < ${profile.minTapDurationMs}ms)")
            }
        }

        // 4. Rapid Phantom Burst Detection (Micro-cluster frequency)
        if (raw.actionMasked == MotionEvent.ACTION_DOWN) {
            cleanOldDownEvents(raw.eventTime)
            recentDownEvents.addLast(raw)
            val burstWindowEvents = recentDownEvents.filter {
                (raw.eventTime - it.eventTime) <= profile.burstWindowMs &&
                        hypot(raw.x - it.x, raw.y - it.y) <= (profile.maxJitterRadiusPx * 2)
            }
            if (burstWindowEvents.size >= profile.burstThresholdCount) {
                score += 60
                category = TouchCategory.GHOST_MICRO_BURST
                suppressionReasons.add("Rapid phantom cluster burst (${burstWindowEvents.size} taps in ${profile.burstWindowMs}ms)")
            }
        }

        // 5. Unnatural Multi-Touch Spawns (Phantom opposite corner touches)
        if (raw.pointerCount >= 3 && raw.actionMasked == MotionEvent.ACTION_POINTER_DOWN) {
            if (raw.pressure < 0.05f) {
                score += 40
                category = TouchCategory.GHOST_UNNATURAL_MULTITOUCH
                suppressionReasons.add("Zero-pressure multi-pointer spawn")
            }
        }

        // 6. Context-Aware Protection for Legitimate Interactions
        // If legitimate gesture: smooth velocity, normal duration, consistent pressure
        if (raw.actionMasked == MotionEvent.ACTION_MOVE && calculatedVelocity in 0.1f..4.0f && raw.pressure in 0.15f..0.95f) {
            // Reward legitimate drag
            score = (score - 30).coerceAtLeast(0)
            if (category == TouchCategory.UNKNOWN) {
                category = TouchCategory.LEGITIMATE_DRAG
            }
        }

        if (raw.actionMasked == MotionEvent.ACTION_UP && raw.durationMs >= 40L && score < 30) {
            category = TouchCategory.LEGITIMATE_TAP
        }

        // Bound score
        score = score.coerceIn(0, 100)
        val isSuppressed = score >= profile.anomalyScoreThreshold && profile.type != ProfileType.DIAGNOSTIC

        // Update history
        if (raw.actionMasked == MotionEvent.ACTION_UP || raw.actionMasked == MotionEvent.ACTION_CANCEL) {
            pointerHistories.remove(raw.pointerId)
        } else {
            history.add(raw)
            if (history.size > 15) {
                history.removeAt(0)
            }
        }

        // Determine severity & emit anomaly if score is high
        if (score >= 40) {
            val severity = when {
                score >= 80 -> AnomalySeverity.CRITICAL
                score >= 65 -> AnomalySeverity.HIGH
                score >= 50 -> AnomalySeverity.MEDIUM
                else -> AnomalySeverity.LOW
            }
            _currentThreatLevel.value = severity

            val anomaly = GhostTouchAnomaly(
                timestamp = raw.eventTime,
                x = raw.x,
                y = raw.y,
                anomalyScore = score,
                severity = severity,
                category = category,
                description = suppressionReasons.joinToString("; "),
                burstCount = 1,
                deltaDurationMs = raw.durationMs,
                matchedDeadZoneId = matchedDeadZone?.id
            )
            _anomalyFlow.tryEmit(anomaly)
        }

        return FilteredTouchEvent(
            raw = raw,
            anomalyScore = score,
            isSuppressed = isSuppressed,
            suppressionReason = suppressionReasons.firstOrNull(),
            category = category,
            calculatedVelocity = calculatedVelocity,
            jitterRadius = jitterRadius
        )
    }

    private fun findMatchingDeadZone(x: Float, y: Float): DeadZoneConfig? {
        synchronized(deadZones) {
            return deadZones.firstOrNull { it.contains(x, y, screenWidth, screenHeight) }
        }
    }

    private fun cleanOldDownEvents(currentTime: Long) {
        while (recentDownEvents.isNotEmpty() && (currentTime - recentDownEvents.first().eventTime) > 1000L) {
            recentDownEvents.removeFirst()
        }
    }

    fun resetStats() {
        _totalEventsCount.value = 0L
        _suppressedEventsCount.value = 0L
        _currentThreatLevel.value = AnomalySeverity.LOW
        pointerHistories.clear()
        recentDownEvents.clear()
    }
}
