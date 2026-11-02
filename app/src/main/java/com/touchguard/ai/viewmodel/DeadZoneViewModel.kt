package com.touchguard.ai.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.touchguard.ai.TouchGuardApplication
import com.touchguard.ai.core.model.DeadZoneConfig
import com.touchguard.ai.core.model.DeadZoneMode
import com.touchguard.ai.data.local.DeadZoneEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class DeadZoneViewModel(application: Application) : AndroidViewModel(application) {

    private val db = (application as TouchGuardApplication).database

    val deadZones = db.deadZoneDao().getAllFlow().stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    // Draft bounds for interactive drag-to-calibrate screen (0.0 to 1.0)
    private val _draftLeftRatio = MutableStateFlow(0.2f)
    val draftLeftRatio: StateFlow<Float> = _draftLeftRatio.asStateFlow()

    private val _draftTopRatio = MutableStateFlow(0.3f)
    val draftTopRatio: StateFlow<Float> = _draftTopRatio.asStateFlow()

    private val _draftRightRatio = MutableStateFlow(0.8f)
    val draftRightRatio: StateFlow<Float> = _draftRightRatio.asStateFlow()

    private val _draftBottomRatio = MutableStateFlow(0.6f)
    val draftBottomRatio: StateFlow<Float> = _draftBottomRatio.asStateFlow()

    fun updateDraftBounds(left: Float, top: Float, right: Float, bottom: Float) {
        _draftLeftRatio.value = left.coerceIn(0f, 0.95f)
        _draftTopRatio.value = top.coerceIn(0f, 0.95f)
        _draftRightRatio.value = right.coerceIn(_draftLeftRatio.value + 0.05f, 1f)
        _draftBottomRatio.value = bottom.coerceIn(_draftTopRatio.value + 0.05f, 1f)
    }

    fun saveDraftAsDeadZone(name: String, mode: DeadZoneMode = DeadZoneMode.BLOCK_TOUCH) {
        viewModelScope.launch {
            val entity = DeadZoneEntity(
                name = name.ifBlank { "Custom Calibrated Box" },
                leftRatio = _draftLeftRatio.value,
                topRatio = _draftTopRatio.value,
                rightRatio = _draftRightRatio.value,
                bottomRatio = _draftBottomRatio.value,
                mode = mode.name,
                isEnabled = true,
                incidentCount = 0,
                colorHex = "#FF1744"
            )
            db.deadZoneDao().insert(entity)
        }
    }

    fun toggleZoneEnabled(entity: DeadZoneEntity) {
        viewModelScope.launch {
            db.deadZoneDao().update(entity.copy(isEnabled = !entity.isEnabled))
        }
    }

    fun toggleZoneMode(entity: DeadZoneEntity) {
        viewModelScope.launch {
            val currentMode = try { DeadZoneMode.valueOf(entity.mode) } catch (_: Exception) { DeadZoneMode.BLOCK_TOUCH }
            val newMode = if (currentMode == DeadZoneMode.BLOCK_TOUCH) DeadZoneMode.MONITOR_ONLY else DeadZoneMode.BLOCK_TOUCH
            db.deadZoneDao().update(entity.copy(mode = newMode.name))
        }
    }

    fun deleteZone(id: Long) {
        viewModelScope.launch {
            db.deadZoneDao().deleteById(id)
        }
    }

    fun addPreset(config: DeadZoneConfig) {
        viewModelScope.launch {
            db.deadZoneDao().insert(DeadZoneEntity.fromConfig(config))
        }
    }
}
