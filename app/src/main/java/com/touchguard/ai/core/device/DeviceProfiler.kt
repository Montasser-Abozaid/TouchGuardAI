package com.touchguard.ai.core.device

import android.content.Context
import android.content.res.Resources
import android.os.Build
import android.util.DisplayMetrics
import android.view.InputDevice
import android.view.WindowManager
import com.touchguard.ai.core.model.DeviceProfile
import com.touchguard.ai.core.model.InputDeviceInfo
import java.io.File

object DeviceProfiler {

    fun profileDevice(context: Context): DeviceProfile {
        val wm = context.getSystemService(Context.WINDOW_SERVICE) as? WindowManager
        val metrics = DisplayMetrics()
        var refreshRate = 60.0f
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val bounds = wm?.currentWindowMetrics?.bounds
            val width = bounds?.width() ?: Resources.getSystem().displayMetrics.widthPixels
            val height = bounds?.height() ?: Resources.getSystem().displayMetrics.heightPixels
            val density = Resources.getSystem().displayMetrics.densityDpi
            refreshRate = context.display?.refreshRate ?: 60.0f
            return buildProfile(context, width, height, density, refreshRate)
        } else {
            @Suppress("DEPRECATION")
            wm?.defaultDisplay?.getRealMetrics(metrics)
            @Suppress("DEPRECATION")
            refreshRate = wm?.defaultDisplay?.refreshRate ?: 60.0f
            return buildProfile(context, metrics.widthPixels, metrics.heightPixels, metrics.densityDpi, refreshRate)
        }
    }

    private fun buildProfile(
        context: Context,
        screenWidth: Int,
        screenHeight: Int,
        densityDpi: Int,
        refreshRate: Float
    ): DeviceProfile {
        val model = Build.MODEL ?: "Unknown"
        val manufacturer = Build.MANUFACTURER ?: "Unknown"
        val brand = Build.BRAND ?: "Unknown"
        val deviceCode = Build.DEVICE ?: "Unknown"
        val board = Build.BOARD ?: "Unknown"
        val hardware = Build.HARDWARE ?: "Unknown"

        // Detect Kirin 710 / HiSilicon platform
        val socKirin710 = hardware.contains("kirin710", ignoreCase = true) ||
                board.contains("hi6250", ignoreCase = true) ||
                hardware.contains("hi6250", ignoreCase = true)

        // Detect Honor 8X (JSN-L21, JSN-L22, JSN-L23, JSN-L42, JSN-AL00, JSN-TL00)
        val isHonor8X = model.contains("JSN-", ignoreCase = true) ||
                model.contains("Honor 8X", ignoreCase = true) ||
                deviceCode.contains("HWJSN", ignoreCase = true)

        val emuiVersion = getEmuiVersion()
        val inputDevices = scanInputDevices()
        val devNodes = scanDevInputNodes()
        val driverSummary = probeTouchDriver(inputDevices)

        return DeviceProfile(
            model = model,
            manufacturer = manufacturer,
            brand = brand,
            deviceCode = deviceCode,
            hardwareBoard = "$board / $hardware",
            socKirin710Detected = socKirin710,
            isHonor8XSeries = isHonor8X,
            androidVersion = Build.VERSION.RELEASE ?: "Unknown",
            apiLevel = Build.VERSION.SDK_INT,
            emuiVersion = emuiVersion,
            screenWidthPx = screenWidth,
            screenHeightPx = screenHeight,
            screenDensityDpi = densityDpi,
            refreshRateHz = refreshRate,
            inputDevices = inputDevices,
            devInputNodesFound = devNodes,
            touchDriverSummary = driverSummary
        )
    }

    private fun getEmuiVersion(): String {
        return try {
            val systemPropertiesClass = Class.forName("android.os.SystemProperties")
            val getMethod = systemPropertiesClass.getMethod("get", String::class.java, String::class.java)
            val emuiVer = getMethod.invoke(null, "ro.build.version.emui", "") as? String
            if (!emuiVer.isNullOrBlank()) {
                emuiVer
            } else {
                val hwVer = getMethod.invoke(null, "hwouc.hwpatch.version", "") as? String
                if (!hwVer.isNullOrBlank()) hwVer else "EMUI Undetected / AOSP"
            }
        } catch (_: Exception) {
            "Not Available (Non-Huawei/EMUI or Restricted)"
        }
    }

    private fun scanInputDevices(): List<InputDeviceInfo> {
        val list = mutableListOf<InputDeviceInfo>()
        val deviceIds = InputDevice.getDeviceIds()
        for (id in deviceIds) {
            val dev = InputDevice.getDevice(id) ?: continue
            val sources = mutableListOf<String>()
            val s = dev.sources
            if (s and InputDevice.SOURCE_TOUCHSCREEN == InputDevice.SOURCE_TOUCHSCREEN) sources.add("TOUCHSCREEN")
            if (s and InputDevice.SOURCE_STYLUS == InputDevice.SOURCE_STYLUS) sources.add("STYLUS")
            if (s and InputDevice.SOURCE_TOUCHPAD == InputDevice.SOURCE_TOUCHPAD) sources.add("TOUCHPAD")
            if (s and InputDevice.SOURCE_MOUSE == InputDevice.SOURCE_MOUSE) sources.add("MOUSE")
            if (s and InputDevice.SOURCE_KEYBOARD == InputDevice.SOURCE_KEYBOARD) sources.add("KEYBOARD")

            val motionRanges = mutableListOf<String>()
            dev.motionRanges.forEach { range ->
                motionRanges.add("${range.axis}: [${range.min} .. ${range.max}] res=${range.resolution}")
            }

            list.add(
                InputDeviceInfo(
                    id = dev.id,
                    name = dev.name,
                    sources = sources,
                    isExternal = dev.isExternal,
                    hasVibrator = dev.vibrator.hasVibrator(),
                    motionRanges = motionRanges
                )
            )
        }
        return list
    }

    private fun scanDevInputNodes(): List<String> {
        val found = mutableListOf<String>()
        try {
            val inputDir = File("/dev/input")
            if (inputDir.exists() && inputDir.isDirectory) {
                inputDir.listFiles()?.forEach { file ->
                    val perms = (if (file.canRead()) "r" else "-") + (if (file.canWrite()) "w" else "-")
                    found.add("${file.name} ($perms)")
                }
            }
        } catch (_: Exception) {
            found.add("SELinux restricted /dev/input traversal")
        }
        if (found.isEmpty()) {
            found.add("Accessible only via Android InputManager framework")
        }
        return found
    }

    private fun probeTouchDriver(devices: List<InputDeviceInfo>): String {
        val touchDevices = devices.filter { it.sources.contains("TOUCHSCREEN") }
        if (touchDevices.isEmpty()) {
            return "Generic Virtual Touchscreen / Framework Emulated"
        }
        val names = touchDevices.map { it.name }
        return when {
            names.any { it.contains("synaptics", true) } -> "Synaptics ClearPad Touch IC Driver"
            names.any { it.contains("goodix", true) } -> "Goodix GT-Series Capacitive Touch Driver"
            names.any { it.contains("focaltech", true) || it.contains("ft5", true) } -> "FocalTech FT-Series Controller"
            names.any { it.contains("novatek", true) } -> "Novatek Touch IC"
            else -> "Hardware Controller: ${names.joinToString(", ")}"
        }
    }
}
