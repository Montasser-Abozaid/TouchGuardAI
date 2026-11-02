package com.touchguard.ai.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "incident_logs")
data class IncidentLogEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val timestamp: Long,
    val x: Float,
    val y: Float,
    val anomalyScore: Int,
    val severity: String,
    val category: String,
    val description: String,
    val burstCount: Int,
    val deltaDurationMs: Long
)
