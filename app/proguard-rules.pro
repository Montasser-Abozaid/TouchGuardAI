# Room rules
-keep class * extends androidx.room.RoomDatabase
-dontwarn androidx.room.paging.**

# Compose rules
-keepclassmembers class * extends androidx.compose.ui.Modifier { *; }

# Models
-keep class com.touchguard.ai.core.model.** { *; }
-keep class com.touchguard.ai.data.local.** { *; }
