package com.touchguard.ai.service

import android.accessibilityservice.AccessibilityService
import android.os.Build
import android.view.accessibility.AccessibilityEvent
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class TouchGuardAccessibilityService : AccessibilityService() {

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return
        _lastEventSummary.value = "Type: ${event.eventType}, Pkg: ${event.packageName}, Class: ${event.className}"
    }

    override fun onInterrupt() {
        _serviceConnected.value = false
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        _serviceConnected.value = true
        _serviceFeaturesReport.value = buildResearchReport()
    }

    private fun buildResearchReport(): String {
        val sb = StringBuilder()
        sb.append("Accessibility Bridge Assessment:\n")
        sb.append("- Window Interception: Active\n")
        sb.append("- TYPE_ACCESSIBILITY_OVERLAY supported: ${Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1}\n")
        sb.append("- Global Touch Sniffing without Consume: NOT PERMITTED BY AOSP SANDBOX\n")
        sb.append("- Selective Touch Forwarding: CANNOT FORWARD AFTER CONSUME WITHOUT INJECT_EVENTS\n")
        sb.append("- Touch Exploration Mode: Intentionally Disabled to prevent talkback double-tap disruption\n")
        return sb.toString()
    }

    override fun onDestroy() {
        super.onDestroy()
        _serviceConnected.value = false
    }

    companion object {
        private val _serviceConnected = MutableStateFlow(false)
        val serviceConnected = _serviceConnected.asStateFlow()

        private val _lastEventSummary = MutableStateFlow("No accessibility events captured")
        val lastEventSummary = _lastEventSummary.asStateFlow()

        private val _serviceFeaturesReport = MutableStateFlow("Accessibility Service Not Bound")
        val serviceFeaturesReport = _serviceFeaturesReport.asStateFlow()
    }
}
