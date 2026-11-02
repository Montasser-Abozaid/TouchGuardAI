package com.touchguard.ai.viewmodel

import android.view.MotionEvent
import androidx.lifecycle.ViewModel
import com.touchguard.ai.core.model.FilteredTouchEvent
import com.touchguard.ai.core.model.RawMotionEvent
import com.touchguard.ai.engine.TouchAnalysisEngine
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class SimulationResult(
    val testName: String,
    val injectedCount: Int,
    val suppressedCount: Int,
    val maxAnomalyScore: Int,
    val verdict: String,
    val isSuccess: Boolean
)

class SimulatorViewModel : ViewModel() {

    private val testEngine = TouchAnalysisEngine()

    private val _simResults = MutableStateFlow<List<SimulationResult>>(emptyList())
    val simResults: StateFlow<List<SimulationResult>> = _simResults.asStateFlow()

    private val _lastSimulatedEvent = MutableStateFlow<FilteredTouchEvent?>(null)
    val lastSimulatedEvent: StateFlow<FilteredTouchEvent?> = _lastSimulatedEvent.asStateFlow()

    fun runPhantomSingleTap() {
        val now = System.currentTimeMillis()
        val rawDown = RawMotionEvent(
            action = MotionEvent.ACTION_DOWN,
            actionMasked = MotionEvent.ACTION_DOWN,
            pointerId = 0,
            x = 450f,
            y = 800f,
            pressure = 0.02f,
            size = 0.01f,
            eventTime = now,
            downTime = now
        )
        val rawUp = RawMotionEvent(
            action = MotionEvent.ACTION_UP,
            actionMasked = MotionEvent.ACTION_UP,
            pointerId = 0,
            x = 450.5f,
            y = 800.5f,
            pressure = 0.01f,
            size = 0.01f,
            eventTime = now + 4L, // 4ms duration!
            downTime = now
        )

        testEngine.analyzeEvent(rawDown)
        val filteredUp = testEngine.analyzeEvent(rawUp)
        _lastSimulatedEvent.value = filteredUp

        val passed = filteredUp.isSuppressed || filteredUp.anomalyScore >= 50
        addResult(
            SimulationResult(
                testName = "Sub-Frame Phantom Tap (4ms pulse)",
                injectedCount = 2,
                suppressedCount = if (filteredUp.isSuppressed) 1 else 0,
                maxAnomalyScore = filteredUp.anomalyScore,
                verdict = if (passed) "DETECTED & SCORED HIGH ($filteredUp.anomalyScore/100)" else "MISSED",
                isSuccess = passed
            )
        )
    }

    fun runRapidMicroBurst() {
        var baseTime = System.currentTimeMillis()
        var suppressed = 0
        var maxScore = 0

        // Inject 12 rapid taps in 80ms within 3px radius
        for (i in 0 until 12) {
            val down = RawMotionEvent(
                action = MotionEvent.ACTION_DOWN,
                actionMasked = MotionEvent.ACTION_DOWN,
                pointerId = 0,
                x = 300f + (i % 3),
                y = 500f + (i % 2),
                pressure = 0.1f,
                size = 0.02f,
                eventTime = baseTime,
                downTime = baseTime
            )
            val filteredDown = testEngine.analyzeEvent(down)
            if (filteredDown.isSuppressed) suppressed++
            maxScore = maxOf(maxScore, filteredDown.anomalyScore)

            val up = RawMotionEvent(
                action = MotionEvent.ACTION_UP,
                actionMasked = MotionEvent.ACTION_UP,
                pointerId = 0,
                x = 300f + (i % 3),
                y = 500f + (i % 2),
                pressure = 0.05f,
                size = 0.02f,
                eventTime = baseTime + 5L,
                downTime = baseTime
            )
            val filteredUp = testEngine.analyzeEvent(up)
            if (filteredUp.isSuppressed) suppressed++
            maxScore = maxOf(maxScore, filteredUp.anomalyScore)
            _lastSimulatedEvent.value = filteredUp

            baseTime += 7L // 7ms between taps (impossible for human fingers)
        }

        val success = suppressed >= 6
        addResult(
            SimulationResult(
                testName = "Rapid Phantom Burst (12 taps / 80ms)",
                injectedCount = 24,
                suppressedCount = suppressed,
                maxAnomalyScore = maxScore,
                verdict = if (success) "BLOCKED PHANTOM CLUSTER ($suppressed suppressed)" else "BURST NOT BLOCKED",
                isSuccess = success
            )
        )
    }

    fun runErraticJump() {
        val now = System.currentTimeMillis()
        val p1 = RawMotionEvent(
            action = MotionEvent.ACTION_DOWN,
            actionMasked = MotionEvent.ACTION_DOWN,
            pointerId = 0,
            x = 100f,
            y = 100f,
            pressure = 0.5f,
            size = 0.1f,
            eventTime = now,
            downTime = now
        )
        testEngine.analyzeEvent(p1)

        // Teleport 1200px in 3ms
        val p2 = RawMotionEvent(
            action = MotionEvent.ACTION_MOVE,
            actionMasked = MotionEvent.ACTION_MOVE,
            pointerId = 0,
            x = 900f,
            y = 1800f,
            pressure = 0.5f,
            size = 0.1f,
            eventTime = now + 3L,
            downTime = now
        )
        val filtered = testEngine.analyzeEvent(p2)
        _lastSimulatedEvent.value = filtered

        val success = filtered.isSuppressed || filtered.anomalyScore >= 50
        addResult(
            SimulationResult(
                testName = "Erratic Teleport Jump (1400px in 3ms)",
                injectedCount = 2,
                suppressedCount = if (filtered.isSuppressed) 1 else 0,
                maxAnomalyScore = filtered.anomalyScore,
                verdict = if (success) "FLAGGED IMPOSSIBLE ACCELERATION ($filtered.anomalyScore/100)" else "MISSED JUMP",
                isSuccess = success
            )
        )
    }

    fun runNormalSwipeSimulation() {
        var baseTime = System.currentTimeMillis()
        var suppressed = 0
        var maxScore = 0

        val down = RawMotionEvent(
            action = MotionEvent.ACTION_DOWN,
            actionMasked = MotionEvent.ACTION_DOWN,
            pointerId = 0,
            x = 200f,
            y = 1200f,
            pressure = 0.65f,
            size = 0.12f,
            eventTime = baseTime,
            downTime = baseTime
        )
        testEngine.analyzeEvent(down)

        // 10 smooth moves over 200ms
        for (step in 1..10) {
            baseTime += 20L
            val move = RawMotionEvent(
                action = MotionEvent.ACTION_MOVE,
                actionMasked = MotionEvent.ACTION_MOVE,
                pointerId = 0,
                x = 200f + (step * 25f), // 250px total smooth glide
                y = 1200f - (step * 35f),
                pressure = 0.65f,
                size = 0.12f,
                eventTime = baseTime,
                downTime = baseTime - (step * 20L)
            )
            val filtered = testEngine.analyzeEvent(move)
            if (filtered.isSuppressed) suppressed++
            maxScore = maxOf(maxScore, filtered.anomalyScore)
            _lastSimulatedEvent.value = filtered
        }

        val up = RawMotionEvent(
            action = MotionEvent.ACTION_UP,
            actionMasked = MotionEvent.ACTION_UP,
            pointerId = 0,
            x = 450f,
            y = 850f,
            pressure = 0.4f,
            size = 0.1f,
            eventTime = baseTime + 20L,
            downTime = baseTime - 200L
        )
        val filteredUp = testEngine.analyzeEvent(up)
        _lastSimulatedEvent.value = filteredUp

        // Legitimate swipe MUST NOT BE SUPPRESSED!
        val preserved = (suppressed == 0 && maxScore < 40)
        addResult(
            SimulationResult(
                testName = "Legitimate Human Swipe (250px / 220ms)",
                injectedCount = 12,
                suppressedCount = suppressed,
                maxAnomalyScore = maxScore,
                verdict = if (preserved) "SUCCESSFULLY PRESERVED (Score: $maxScore/100, 0 Suppressed)" else "FALSE POSITIVE (Erroneously Suppressed)",
                isSuccess = preserved
            )
        )
    }

    private fun addResult(result: SimulationResult) {
        val current = _simResults.value.toMutableList()
        current.add(0, result)
        _simResults.value = current
    }

    fun clearResults() {
        _simResults.value = emptyList()
        _lastSimulatedEvent.value = null
    }
}
