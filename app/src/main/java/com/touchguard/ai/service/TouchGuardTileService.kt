package com.touchguard.ai.service

import android.graphics.drawable.Icon
import android.os.Build
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import androidx.annotation.RequiresApi
import com.touchguard.ai.R

@RequiresApi(Build.VERSION_CODES.N)
class TouchGuardTileService : TileService() {

    override fun onStartListening() {
        super.onStartListening()
        updateTileState()
    }

    override fun onClick() {
        super.onClick()
        val tile = qsTile ?: return
        val isActive = (tile.state == Tile.STATE_ACTIVE)

        if (isActive) {
            // Disengage
            TouchGuardForegroundService.stop(this)
            DeadZoneOverlayService.stop(this)
            tile.state = Tile.STATE_INACTIVE
            tile.label = getString(R.string.status_shield_standby)
        } else {
            // Engage
            TouchGuardForegroundService.start(this)
            DeadZoneOverlayService.start(this)
            tile.state = Tile.STATE_ACTIVE
            tile.label = getString(R.string.status_shield_active)
        }
        tile.icon = Icon.createWithResource(this, R.drawable.ic_shield_tile)
        tile.updateTile()
    }

    private fun updateTileState() {
        val tile = qsTile ?: return
        val running = TouchGuardForegroundService.isRunning.value
        tile.state = if (running) Tile.STATE_ACTIVE else Tile.STATE_INACTIVE
        tile.label = if (running) getString(R.string.status_shield_active) else getString(R.string.status_shield_standby)
        tile.icon = Icon.createWithResource(this, R.drawable.ic_shield_tile)
        tile.updateTile()
    }
}
