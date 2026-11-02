package com.touchguard.ai.core.model

data class InputDeviceInfo(
    val id: Int,
    val name: String,
    val sources: List<String>,
    val isExternal: Boolean,
    val hasVibrator: Boolean,
    val motionRanges: List<String>
)

data class DeviceProfile(
    val model: String,
    val manufacturer: String,
    val brand: String,
    val deviceCode: String,
    val hardwareBoard: String,
    val socKirin710Detected: Boolean,
    val isHonor8XSeries: Boolean,
    val androidVersion: String,
    val apiLevel: Int,
    val emuiVersion: String,
    val screenWidthPx: Int,
    val screenHeightPx: Int,
    val screenDensityDpi: Int,
    val refreshRateHz: Float,
    val inputDevices: List<InputDeviceInfo>,
    val devInputNodesFound: List<String>,
    val touchDriverSummary: String
)
