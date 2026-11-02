package com.touchguard.ai.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.touchguard.ai.core.model.DeadZoneConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [IncidentLogEntity::class, DeadZoneEntity::class],
    version = 1,
    exportSchema = false
)
abstract class TouchGuardDatabase : RoomDatabase() {

    abstract fun incidentLogDao(): IncidentLogDao
    abstract fun deadZoneDao(): DeadZoneDao

    companion object {
        @Volatile
        private var INSTANCE: TouchGuardDatabase? = null

        fun getInstance(context: Context): TouchGuardDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    TouchGuardDatabase::class.java,
                    "touchguard_ai.db"
                ).addCallback(object : Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        // Pre-populate with recommended Honor 8X default presets
                        CoroutineScope(Dispatchers.IO).launch {
                            val database = getInstance(context)
                            val presets = listOf(
                                DeadZoneEntity.fromConfig(DeadZoneConfig.createTopEdgePreset()),
                                DeadZoneEntity.fromConfig(DeadZoneConfig.createRightEdgePreset()),
                                DeadZoneEntity.fromConfig(DeadZoneConfig.createLeftEdgePreset())
                            )
                            database.deadZoneDao().insertAll(presets)
                        }
                    }
                }).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
