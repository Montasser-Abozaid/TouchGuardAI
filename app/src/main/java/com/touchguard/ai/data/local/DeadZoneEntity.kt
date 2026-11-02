package com.touchguard.ai.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.touchguard.ai.core.model.DeadZoneConfig
import com.touchguard.ai.core.model.DeadZoneMode

@Entity(tableName = "dead_zones")
data class DeadZoneEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val leftRatio: Float,
    val topRatio: Float,
    val rightRatio: Float,
    val bottomRatio: Float,
    val mode: String,
    val isEnabled: Boolean,
    val incidentCount: Int,
    val colorHex: String
) {
    fun toConfig(): DeadZoneConfig {
        return DeadZoneConfig(
            id = id,
            name = name,
            leftRatio = leftRatio,
            topRatio = topRatio,
            rightRatio = rightRatio,
            bottomRatio = bottomRatio,
            mode = try { DeadZoneMode.valueOf(mode) } catch (_: Exception) { DeadZoneMode.BLOCK_TOUCH },
            isEnabled = isEnabled,
            incidentCount = incidentCount,
            colorHex = colorHex
        )
    }

    companion object {
        fun fromConfig(config: DeadZoneConfig): DeadZoneEntity {
            return DeadZoneEntity(
                id = config.id,
                name = config.name,
                leftRatio = config.leftRatio,
                topRatio = config.topRatio,
                rightRatio = config.rightRatio,
                bottomRatio = config.bottomRatio,
                mode = config.mode.name,
                isEnabled = config.isEnabled,
                incidentCount = config.incidentCount,
                colorHex = config.colorHex
            )
        }
    }
}
