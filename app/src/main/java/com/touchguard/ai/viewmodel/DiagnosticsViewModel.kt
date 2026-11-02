package com.touchguard.ai.viewmodel

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.touchguard.ai.core.model.GhostTouchAnomaly
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class PointerTrack(
    val id: Int,
    val x: Float,
    val y: Float,
    val pressure: Float,
    val size: Float,
    val color: Color
)

class DiagnosticsViewModel : ViewModel() {

    // Grid Test: 12 cols x 20 rows
    val gridCols = 10
    val gridRows = 18
    private val totalCells = gridCols * gridRows
    private val _touchedGridCells = MutableStateFlow<Set<Int>>(emptySet())
    val touchedGridCells: StateFlow<Set<Int>> = _touchedGridCells.asStateFlow()

    // Multi-Touch Test pointers
    private val _activePointers = MutableStateFlow<Map<Int, PointerTrack>>(emptyMap())
    val activePointers: StateFlow<Map<Int, PointerTrack>> = _activePointers.asStateFlow()

    // Autonomous Monitor Test
    private val _isAutonomousRunning = MutableStateFlow(false)
    val isAutonomousRunning: StateFlow<Boolean> = _isAutonomousRunning.asStateFlow()

    private val _autonomousCountdown = MutableStateFlow(30)
    val autonomousCountdown: StateFlow<Int> = _autonomousCountdown.asStateFlow()

    private val _detectedGhostTouches = MutableStateFlow<List<GhostTouchAnomaly>>(emptyList())
    val detectedGhostTouches: StateFlow<List<GhostTouchAnomaly>> = _detectedGhostTouches.asStateFlow()

    private var autonomousJob: Job? = null

    // Heatmap accumulated touch points
    private val _heatmapPoints = MutableStateFlow<List<Pair<Float, Float>>>(emptyList())
    val heatmapPoints: StateFlow<List<Pair<Float, Float>>> = _heatmapPoints.asStateFlow()

    fun markGridCell(col: Int, row: Int) {
        if (col in 0 until gridCols && row in 0 until gridRows) {
            val index = row * gridCols + col
            if (!_touchedGridCells.value.contains(index)) {
                _touchedGridCells.value = _touchedGridCells.value + index
            }
        }
    }

    fun getGridCoveragePercentage(): Int {
        return ((_touchedGridCells.value.size.toFloat() / totalCells) * 100).toInt()
    }

    fun resetGrid() {
        _touchedGridCells.value = emptySet()
    }

    fun updatePointer(id: Int, x: Float, y: Float, pressure: Float, size: Float) {
        val current = _activePointers.value.toMutableMap()
        val palette = listOf(
            Color(0xFF00F0FF), Color(0xFF00E676), Color(0xFFFF1744),
            Color(0xFFFFAB00), Color(0xFFE040FB), Color(0xFF7C4DFF),
            Color(0xFFFF5252), Color(0xFF69F0AE), Color(0xFFFFD740), Color(0xFF40C4FF)
        )
        val color = palette[id % palette.size]
        current[id] = PointerTrack(id, x, y, pressure, size, color)
        _activePointers.value = current

        // Accumulate to heatmap
        val heat = _heatmapPoints.value.toMutableList()
        if (heat.size > 500) heat.removeAt(0)
        heat.add(Pair(x, y))
        _heatmapPoints.value = heat
    }

    fun removePointer(id: Int) {
        val current = _activePointers.value.toMutableMap()
        current.remove(id)
        _activePointers.value = current
    }

    fun clearAllPointers() {
        _activePointers.value = emptyMap()
    }

    fun startAutonomousTest(durationSeconds: Int = 30) {
        autonomousJob?.cancel()
        _isAutonomousRunning.value = true
        _autonomousCountdown.value = durationSeconds
        _detectedGhostTouches.value = emptyList()

        autonomousJob = viewModelScope.launch {
            for (sec in durationSeconds downTo 1) {
                _autonomousCountdown.value = sec
                delay(1000)
            }
            _isAutonomousRunning.value = false
        }
    }

    fun stopAutonomousTest() {
        autonomousJob?.cancel()
        _isAutonomousRunning.value = false
    }

    fun recordAutonomousGhostTouch(anomaly: GhostTouchAnomaly) {
        if (_isAutonomousRunning.value) {
            _detectedGhostTouches.value = _detectedGhostTouches.value + anomaly
        }
    }

    fun clearHeatmap() {
        _heatmapPoints.value = emptyList()
    }
}
