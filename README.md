# TOUCH GUARD AI
## Advanced Android Ghost Touch Detection & Digitizer Protection System

**Touch Guard AI** is an advanced Android system designed to diagnose, detect, and mitigate physical Ghost Touch anomalies caused by damaged or failing touchscreen digitizers. Specifically tuned and optimized for the **Huawei Honor 8X** (Kirin 710 SoC, EMUI 8.2 / 9.0 / 10.0), it combines a stateful Kotlin touch analysis engine with hardware profiling, interactive diagnostics, and smart dead-zone shielding.

---

### Project Structure & Modular Architecture

```
d:/GAMES/TouchGuardAI/
├── app/
│   ├── build.gradle.kts                   # Modern Kotlin DSL build configuration
│   ├── proguard-rules.pro                 # Release shrinking & keep rules
│   └── src/main/
│       ├── AndroidManifest.xml            # Service declarations & zero-network privacy policy
│       ├── java/com/touchguard/ai/
│       │   ├── TouchGuardApplication.kt   # Room DB init & notification channels
│       │   ├── core/
│       │   │   ├── device/
│       │   │   │   └── DeviceProfiler.kt  # Honor 8X, Kirin 710, EMUI & /dev/input inspector
│       │   │   └── model/
│       │   │       ├── DeadZoneConfig.kt  # Dead zone geometry & presets
│       │   │       ├── DeviceProfile.kt   # System & display hardware profile
│       │   │       ├── GhostTouchAnomaly.kt # Anomaly severity & scoring data
│       │   │       ├── ProtectionProfile.kt # Preset thresholds (Normal, Sensitive, etc.)
│       │   │       └── TouchEventData.kt  # Raw & filtered motion event structures
│       │   ├── data/local/                # Room Database for logs & dead zones
│       │   │   ├── DeadZoneDao.kt
│       │   │   ├── DeadZoneEntity.kt
│       │   │   ├── IncidentLogDao.kt
│       │   │   ├── IncidentLogEntity.kt
│       │   │   └── TouchGuardDatabase.kt
│       │   ├── engine/
│       │   │   ├── TouchAnalysisEngine.kt # Stateful trajectory, burst, & scoring engine
│       │   │   └── TouchGuardInterceptLayout.kt # App-level ViewGroup event interrupter
│       │   ├── service/
│       │   │   ├── DeadZoneOverlayService.kt # Floating dead zone shield & killswitch
│       │   │   ├── TouchGuardAccessibilityService.kt # Window monitoring & research probe
│       │   │   ├── TouchGuardForegroundService.kt # Sticky telemetry notification
│       │   │   └── TouchGuardTileService.kt # Quick Settings shade toggle
│       │   ├── ui/
│       │   │   ├── MainActivity.kt        # Compose NavHost & navigation
│       │   │   ├── dashboard/             # Cyber Shield Dashboard & telemetry
│       │   │   │   ├── ActivityGraph.kt
│       │   │   │   ├── DashboardScreen.kt
│       │   │   │   └── ShieldIndicator.kt
│       │   │   ├── deadzone/              # Dead zone manager & visual drag calibration
│       │   │   │   ├── DeadZoneManagerScreen.kt
│       │   │   │   └── InteractiveCalibrationScreen.kt
│       │   │   ├── diagnostics/           # Touchscreen diagnostic suites
│       │   │   │   ├── AutonomousGhostDetectorScreen.kt
│       │   │   │   ├── DeadPixelTestScreen.kt
│       │   │   │   ├── DiagnosticsScreen.kt
│       │   │   │   ├── HeatmapCanvas.kt
│       │   │   │   ├── LinearTestCanvas.kt
│       │   │   │   ├── MultiTouchCanvas.kt
│       │   │   │   └── TouchGridCanvas.kt
│       │   │   ├── navigation/Screen.kt
│       │   │   ├── profiles/ProfilesScreen.kt
│       │   │   ├── reports/SystemResearchScreen.kt
│       │   │   ├── simulator/TouchSimulatorScreen.kt
│       │   │   └── theme/                 # Dark Navy & Cyan glassmorphism theme
│       │   │       ├── Color.kt
│       │   │       ├── Glassmorphic.kt
│       │   │       └── Theme.kt
│       │   └── viewmodel/                 # MVVM StateFlow ViewModels
│       │       ├── DeadZoneViewModel.kt
│       │       ├── DiagnosticsViewModel.kt
│       │       ├── MainViewModel.kt
│       │       └── SimulatorViewModel.kt
│       └── res/
│           ├── values/strings.xml         # Full English localization
│           ├── values-ar/strings.xml      # Full Arabic localization
│           ├── values/colors.xml
│           ├── values/themes.xml
│           └── xml/accessibility_service_config.xml
├── gradle/
│   ├── libs.versions.toml                 # Version catalog
│   └── wrapper/gradle-wrapper.properties
├── simulator_web/
│   └── index.html                         # Interactive Web Companion & Fault Injector
├── build.gradle.kts
├── settings.gradle.kts
├── HONOR_8X_TOUCH_RESEARCH_REPORT.md      # Comprehensive technical research report
└── README.md
```

---

### Technical Reality & Status Matrix

| Feature Module | Status | Technical Reality & Reason |
| :--- | :--- | :--- |
| **Phase 1: Hardware Profiler** | **IMPLEMENTED** | Detects Honor 8X (`JSN-*`), Kirin 710 (`hi6250`), EMUI version, display resolution ($1080 \times 2340$), refresh rate ($60\text{Hz}$), and touch input nodes. |
| **Phase 2: Touch Analysis Engine** | **IMPLEMENTED** | Analyzes $\Delta t$, velocity, micro-clustering, stationary micro-tremor, and calculates anomaly scores ($0-100$). Protects legitimate swipes and typing. |
| **Phase 3: Level 1 (App-Level)** | **IMPLEMENTED** | Custom `ViewGroup` overrides `dispatchTouchEvent()` to drop phantom clicks within Touch Guard UI. |
| **Phase 3: Level 2 (Dead Zone Overlay)** | **IMPLEMENTED** | Uses `WindowManager` with `FLAG_NOT_TOUCH_MODAL` to isolate damaged screen areas from underlying apps. |
| **Phase 3: Level 2 (Global Sniffing)** | **PARTIALLY IMPLEMENTED (BOUNDED)** | Dead zones block touches over damaged spots; transparent global touch forwarding is prohibited by Android's sandbox security model. |
| **Phase 3: Level 3 (System InputFilter)** | **NOT POSSIBLE ON STOCK** | Requires `android.permission.INJECT_EVENTS` (Signature/Root). Bootloader is locked on stock Kirin 710 Honor 8X. Full architecture is documented. |
| **Phase 4: Smart Dead Zone** | **IMPLEMENTED** | Visual drag-to-calibrate bounding box, presets (Notch, Lateral Edges), and instant emergency killswitch. |
| **Phase 5: Diagnostic Suite** | **IMPLEMENTED** | Touch Grid, 10-Point Multi-Touch, Linearity Guide, Dead Pixel pure color wash, Heatmap, and Hands-Free Autonomous Monitor. |
| **Phase 6: Protection Profiles** | **IMPLEMENTED** | Normal, Sensitive Screen, Damaged Edge, Damaged Zone, and Diagnostic modes with adjustable thresholds. |
| **Phase 7: Cyber Dashboard UI** | **IMPLEMENTED** | Jetpack Compose, Dark Navy (`#070B19`), Electric Blue, Cyan Glassmorphism, animated pulsing shield, and live telemetry graphs. |
| **Phase 8: Services & Tile** | **IMPLEMENTED** | Foreground Service with sticky notification, Quick Settings Tile (`TouchGuardTileService`), and floating kill button. |
| **Phase 9: Privacy & Offline First** | **IMPLEMENTED** | Zero internet permissions in manifest (`android.permission.INTERNET` omitted), 100% offline, zero data transmission. |
| **Phase 11: Touch Simulator** | **IMPLEMENTED** | In-app validator simulating phantom bursts, sub-frame taps, and erratic jumps to prove zero false positives. |

---

### Installation & Build Guide

#### 1. Building the APK with Android Studio
1. Open **Android Studio** (Hedgehog 2023.1.1 or Iguana/Jellyfish).
2. Select **Open** and choose `d:\GAMES\TouchGuardAI`.
3. Wait for Gradle sync to complete using the included Gradle Wrapper (`gradle-8.6-bin.zip`).
4. Select `Build > Build Bundle(s) / APK(s) > Build APK(s)`.
5. The debug APK will be generated at `app/build/outputs/apk/debug/app-debug.apk`.

#### 2. Building from Command Line
```bash
cd d:\GAMES\TouchGuardAI
.\gradlew assembleDebug
```

---

### Step-by-Step Huawei Honor 8X Testing Guide

#### 1. Enable Developer Options & USB Debugging
1. On your Honor 8X, open **Settings > About Phone**.
2. Tap **Build Number** 7 times until you see *"You are now a developer!"*.
3. Go to **Settings > System & Updates > Developer Options**.
4. Enable **USB Debugging**.
5. Connect your phone to your PC via USB cable and authorize debugging.

#### 2. Install and Grant Permissions via ADB
```bash
# Install the APK
adb install -r app/build/outputs/apk/debug/app-debug.apk

# Grant Overlay Permission (Required for Smart Dead Zones)
adb shell appops set com.touchguard.ai SYSTEM_ALERT_WINDOW allow

# Grant Notification Permission (Android 13+)
adb shell pm grant com.touchguard.ai android.permission.POST_NOTIFICATIONS
```

#### 3. Real-Time Hardware Diagnostic via ADB
Inspect the Honor 8X touch sensor IC and display metrics:
```bash
adb shell dumpsys input
```
Sniff raw hardware touch interrupts as they happen:
```bash
adb shell getevent -l
```

#### 4. Testing the In-App Simulator
1. Launch **Touch Guard AI**.
2. Open the **Ghost Touch Simulator** from the dashboard.
3. Tap **"Micro-Burst (50Hz)"** to simulate high-frequency phantom clicking.
4. Verify that the Anomaly Score spikes above 60/100 and flags the cluster.
5. Tap **"Legitimate Swipe"** to verify that normal user swiping is preserved with zero false positives.

#### 5. Testing the Interactive Web Companion
Open `simulator_web/index.html` in any browser to test the scoring algorithm, simulate hardware glitches, and preview the Cyber Shield dashboard immediately.
