package com.touchguard.ai.viewmodel

import android.app.Application
import android.content.Context
import android.os.Build
import android.provider.Settings
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.touchguard.ai.TouchGuardApplication
import com.touchguard.ai.core.device.DeviceProfiler
import com.touchguard.ai.core.model.AnomalySeverity
import com.touchguard.ai.core.model.DeadZoneConfig
import com.touchguard.ai.core.model.DeviceProfile
import com.touchguard.ai.core.model.FilteredTouchEvent
import com.touchguard.ai.core.model.GhostTouchAnomaly
import com.touchguard.ai.core.model.ProtectionProfile
import com.touchguard.ai.data.local.DeadZoneEntity
import com.touchguard.ai.data.local.IncidentLogEntity
import com.touchguard.ai.engine.TouchAnalysisEngine
import com.touchguard.ai.service.DeadZoneOverlayService
import com.touchguard.ai.service.TouchGuardForegroundService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val db = (application as TouchGuardApplication).database
    val engine = TouchAnalysisEngine()

    private val _deviceProfile = MutableStateFlow(DeviceProfiler.profileDevice(application))
    val deviceProfile: StateFlow<DeviceProfile> = _deviceProfile.asStateFlow()

    private val _isShieldActive = MutableStateFlow(false)
    val isShieldActive: StateFlow<Boolean> = _isShieldActive.asStateFlow()

    private val _currentProfile = MutableStateFlow(ProtectionProfile.getPresets()[0])
    val currentProfile: StateFlow<ProtectionProfile> = _currentProfile.asStateFlow()

    val threatLevel: StateFlow<AnomalySeverity> = engine.currentThreatLevel
    val totalTouchEvents: StateFlow<Long> = engine.totalEventsCount
    val filteredGhostEvents: StateFlow<Long> = engine.suppressedEventsCount

    // Telemetry activity graph points (recent 30 sample frequencies)
    private val _activityHistory = MutableStateFlow<List<Float>>(List(30) { 0f })
    val activityHistory: StateFlow<List<Float>> = _activityHistory.asStateFlow()

    // Room DB streams
    val deadZones = db.deadZoneDao().getAllFlow().stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    val incidentLogs = db.incidentLogDao().getRecentIncidents(50).stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    init {
        // Collect anomalies from engine and persist in Room DB
        viewModelScope.launch {
            engine.anomalyFlow.collect { anomaly ->
                db.incidentLogDao().insertIncident(
                    IncidentLogEntity(
                        timestamp = anomaly.timestamp,
                        x = anomaly.x,
                        y = anomaly.y,
                        anomalyScore = anomaly.anomalyScore,
                        severity = anomaly.severity.name,
                        category = anomaly.category.name,
                        description = anomaly.description,
                        burstCount = anomaly.burstCount,
                        deltaDurationMs = anomaly.deltaDurationMs
                    )
                )
                anomaly.matchedDeadZoneId?.let { zoneId ->
                    db.deadZoneDao().incrementIncident(zoneId)
                }
            }
        }

        // Keep engine synced with active dead zones
        viewModelScope.launch {
            deadZones.collect { entities ->
                engine.updateDeadZones(entities.map { it.toConfig() })
            }
        }

        // Feed activity history graph
        viewModelScope.launch {
            engine.recentEventsFlow.collect { filtered ->
                pushActivityPoint(filtered)
            }
        }
    }

    private fun pushActivityPoint(event: FilteredTouchEvent) {
        val current = _activityHistory.value.toMutableList()
        if (current.size >= 30) {
            current.removeAt(0)
        }
        val value = (event.anomalyScore / 100f).coerceIn(0f, 1f)
        current.add(value)
        _activityHistory.value = current
    }

    fun toggleShield(context: Context) {
        val newState = !_isShieldActive.value
        _isShieldActive.value = newState

        if (newState) {
            TouchGuardForegroundService.start(context)
            if (canDrawOverlays(context) && _currentProfile.value.enableDeadZoneOverlays) {
                DeadZoneOverlayService.start(context)
            }
        } else {
            TouchGuardForegroundService.stop(context)
            DeadZoneOverlayService.stop(context)
        }
    }

    fun emergencyKillswitch(context: Context) {
        _isShieldActive.value = false
        TouchGuardForegroundService.stop(context)
        DeadZoneOverlayService.stop(context)
        engine.resetStats()
    }

    fun setProfile(profile: ProtectionProfile) {
        _currentProfile.value = profile
        engine.updateProfile(profile)
    }

    fun clearIncidents() {
        viewModelScope.launch {
            db.incidentLogDao().clearAll()
            engine.resetStats()
        }
    }

    fun canDrawOverlays(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Settings.canDrawOverlays(context)
        } else {
            true
        }
    }
}
