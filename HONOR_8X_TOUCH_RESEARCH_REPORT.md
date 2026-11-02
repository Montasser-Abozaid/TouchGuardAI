# TOUCH GUARD AI
## Technical Architecture & Feasibility Research Report
**Target Device Focus**: Huawei Honor 8X (Kirin 710 | EMUI 8.2 / 9.0 / 10.0 | Android 8.1 - 10)  
**Authoring Team**: Senior Android Engineer, System Framework Engineer, Touchscreen Diagnostics Specialist  

---

### Executive Summary

"Ghost Touch" (phantom touch) on modern capacitive smartphones is a physical-hardware failure resulting from mechanical trauma, micro-fractures in the Indium Tin Oxide (ITO) sensor matrix, liquid seepage, or analog-to-digital converter (ADC) ground reference drift. On the **Huawei Honor 8X** (featuring a 6.5" 2340x1080 LTPS IPS panel bonded to a capacitive digitizer and driven by the **HiSilicon Kirin 710 SoC**), ghost touches manifest as rapid, concentrated pulse bursts, random coordinate jumps, or persistent edge chatter.

This document details the engineering architecture of **Touch Guard AI**, providing an exhaustive assessment of touch control on Android, the three protection tiers, and the technical boundaries of what is achievable on a stock Honor 8X versus rooted/framework-modified environments.

---

### 1. Android Input Subsystem Architecture

To understand where touch events can be intercepted, we must trace the end-to-end journey of an electron from the digitizer glass down to the user interface:

```
[ Physical Touch Screen Digitizer (ITO Grid) ]
                      │ (Capacitive change in pF)
                      ▼
[ Touch Controller IC (e.g., Synaptics / Goodix / FocalTech) ]
                      │ (I2C / SPI Bus interrupts)
                      ▼
[ Linux Kernel Touch Driver (/dev/input/eventX via evdev) ]
                      │ (Kernel input_event structs)
                      ▼
[ Android EventHub (Native C++ in framework/native/services/inputflinger) ]
                      │ (epoll on /dev/input/*)
                      ▼
[ InputReader Thread ]
                      │ (Transforms raw events into MotionEvent objects)
                      ▼
[ InputDispatcher Thread ]
                      │ ──► [ InputFilter Hook (Requires INJECT_EVENTS permission) ]
                      │ (Target window resolution based on WindowManagerService)
                      ▼
[ InputChannel (UNIX Domain Socket Pair per Window) ]
                      │
                      ▼
[ App Process: ViewRootImpl / InputEventReceiver ]
                      │
                      ▼
[ PhoneWindow -> DecorView -> dispatchTouchEvent() -> View Hierarchy ]
```

---

### 2. Three Protection Tiers: Feasibility Matrix

| Protection Level | Mechanism | Stock Honor 8X Feasibility | Required Privileges | Status in Touch Guard AI |
| :--- | :--- | :--- | :--- | :--- |
| **Level 1: App-Level** | Custom `ViewGroup` & `DecorView` interception via `dispatchTouchEvent()` | **100% Possible** | None (Standard User App) | **IMPLEMENTED** |
| **Level 2: Dead Zone Overlays** | Floating `WindowManager` shields with `FLAG_NOT_TOUCH_MODAL` | **100% Possible** | `SYSTEM_ALERT_WINDOW` | **IMPLEMENTED** |
| **Level 2: Global Sniffing** | Transparently filtering other apps' touches | **NOT POSSIBLE** | Restricted by AOSP sandbox | **PARTIALLY IMPLEMENTED (BOUNDED)** |
| **Level 3: System InputFilter** | `InputManager.setInputFilter` hook before `InputDispatcher` | **NOT POSSIBLE (Stock)** | `INJECT_EVENTS` (Signature/Root) | **ARCHITECTED & DOCUMENTED** |

---

### 3. Detailed Technical Analysis of Each Level

#### Level 1: App-Level Protection (STATUS: IMPLEMENTED)
- **Implementation**: `TouchGuardInterceptLayout` intercepts touch streams directly before distributing them to view children.
- **Engine Dynamics**: Analyzes sub-frame impulses ($\Delta t < 12\text{ms}$), impossible velocities ($v > 6.5\text{ px/ms}$), and high-frequency spatial clusters.
- **Result**: Suspicious events are dropped (`return true`), preserving normal UI interaction for genuine user inputs.

#### Level 2: Accessibility & Overlay Exploration (STATUS: PARTIALLY IMPLEMENTED)
- **Dead Zone Isolation (IMPLEMENTED)**:
  By placing a targeted floating overlay (`TYPE_APPLICATION_OVERLAY`) strictly over the damaged section of the digitizer (e.g., top notch, lateral edge, or crack line) configured with `FLAG_NOT_TOUCH_MODAL`, touches inside the overlay are consumed by Touch Guard AI, while touches outside flow directly into underlying apps.
- **Global Transparent Filtering Limitation (EXPLAINED)**:
  In standard Android, an application overlay cannot act as a transparent "filter" that intercepts global touch events, analyzes them, and selectively passes legitimate touches through to underlying apps. Android's security model dictates that an overlay window either **consumes** the event or passes it entirely through via `FLAG_NOT_TOUCHABLE`. Injecting touch events into another app requires `INJECT_EVENTS`, which Android restricts to prevent tapjacking, click fraud, and keystroke logging.
- **Why AccessibilityService Cannot Filter Touches**:
  While `AccessibilityService` can monitor window state changes, it does not intercept raw `MotionEvent` streams transparently. `Touch Exploration Mode` interferes with user gestures by converting taps into double-taps for accessibility speech. Furthermore, `dispatchGesture()` cannot be used as a real-time event router because it introduces latency ($> 30\text{ms}$) and disrupts multi-touch velocity curves.

#### Level 3: Advanced System-Level Protection (STATUS: ARCHITECTED & NOT POSSIBLE ON STOCK)
- **AOSP InputFilter Hook**:
  In AOSP, `InputManagerService.setInputFilter(IInputFilter filter)` allows an input filter to intercept events from `InputReader` before `InputDispatcher` delivers them to windows.
- **The Honor 8X Constraint**:
  The Honor 8X runs on a HiSilicon Kirin 710 processor. In July 2018, Huawei officially terminated its bootloader unlock code service. On a stock Honor 8X with a locked bootloader:
  1. Bootloader cannot be unlocked via fastboot (`fastboot oem unlock` fails).
  2. Sideloading system apps into `/system/priv-app` is blocked by verified boot (dm-verity).
  3. `android.permission.INJECT_EVENTS` cannot be granted to user apps.
- **Architectural Solution for Rooted / Custom ROM Devices**:
  For devices with Root (Magisk) or custom AOSP builds, Touch Guard AI provides the reference `SystemInputFilterBridge` specification to bind directly to `InputManager`.

---

### 4. Smart Dead Zone Calibration & Safety Protocols

#### Operational Principle
When a digitizer suffers localized damage (such as an impact fracture along the bezel), it continuously emits phantom touch packets. The **Smart Dead Zone** feature allows users to draw a boundary box over that area.
- **Trade-off Awareness**: Blocking a dead zone swallows **all** touches (both phantom and legitimate fingers) within that boundary.
- **Emergency Killswitch**:
  To protect users from accidental lockouts, Touch Guard AI incorporates multiple emergency release mechanisms:
  1. **Floating Kill Pill**: A persistent top-level kill button that closes overlays with a single tap.
  2. **Sticky Notification Action**: "Emergency Stop" action button in the persistent notification.
  3. **Quick Settings Tile**: Instant toggle in the Android notification shade.

---

### 5. Step-by-Step Honor 8X Verification & ADB Guide

To verify touchscreen hardware and analyze raw input events on the Honor 8X via ADB:

#### 1. Inspect Hardware Input Nodes
```bash
adb shell dumpsys input
```
Look for the `Touch Input Device` section. On the Honor 8X, this identifies the touch sensor controller, surface orientation, calibration coefficients, and resolution ($1080 \times 2340$).

#### 2. Live Raw Event Sniffing
```bash
adb shell getevent -l
```
This logs kernel evdev events directly. When a ghost touch occurs on the physical phone, you will see rapid bursts of:
```
/dev/input/event2: EV_ABS       ABS_MT_POSITION_X    0000021c
/dev/input/event2: EV_ABS       ABS_MT_POSITION_Y    000003b4
/dev/input/event2: EV_SYN       SYN_REPORT           00000000
```

#### 3. Grant Overlay Permissions via ADB
```bash
adb shell appops set com.touchguard.ai SYSTEM_ALERT_WINDOW allow
```

#### 4. Grant Notification Permissions (Android 13+)
```bash
adb shell pm grant com.touchguard.ai android.permission.POST_NOTIFICATIONS
```

---

### 6. Feature Completion Status Summary

| Feature Module | Status | Technical Details |
| :--- | :--- | :--- |
| **Hardware & System Profiler** | **IMPLEMENTED** | Detects Honor 8X (JSN-*), Kirin 710 SoC, EMUI version, display resolution, refresh rate, and input drivers. |
| **Touch Analysis Engine** | **IMPLEMENTED** | Sub-frame ADC impulse detection, velocity bounds, spatial clustering, and context-aware legitimate gesture protection. |
| **Full Screen Touch Grid** | **IMPLEMENTED** | Interactive 180-cell matrix with real-time coverage calculation and dead zone discovery. |
| **10-Point Multi-Touch Test** | **IMPLEMENTED** | Tracks up to 10 pointers with coordinates, pressure, and touch ellipse sizing. |
| **Autonomous Ghost Detector** | **IMPLEMENTED** | Hands-free 30s unattended monitoring tool that flags and logs phantom contacts. |
| **Dead Pixel vs Dead Zone** | **IMPLEMENTED** | Full-screen pure RGB/OLED color wash cleanly separating panel defects from digitizer defects. |
| **Smart Dead Zone Shield** | **IMPLEMENTED** | Custom draggable calibration canvas with overlay blocking and floating killswitch. |
| **Smart Profiles** | **IMPLEMENTED** | Normal, Sensitive Screen, Damaged Edge, Damaged Zone, and Diagnostic modes. |
| **Ghost Touch Simulator** | **IMPLEMENTED** | In-app validation tool to inject phantom bursts, tremors, and verified legitimate swipes. |
| **Cyber Shield UI / UX** | **IMPLEMENTED** | Dark Navy, Electric Blue, Cyan Glassmorphism, animated shield, and bilingual localization (AR/EN). |
| **Global Touch Sniffer** | **PARTIALLY IMPLEMENTED** | Dead-zone block overlays implemented; transparent global filtering bounded by Android sandbox. |
| **System InputFilter Module** | **NOT POSSIBLE (STOCK)** | Fully architected and documented; requires Root / System Signature on locked Honor 8X. |
