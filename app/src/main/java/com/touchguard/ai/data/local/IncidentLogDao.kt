package com.touchguard.ai.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface IncidentLogDao {

    @Query("SELECT * FROM incident_logs ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentIncidents(limit: Int = 100): Flow<List<IncidentLogEntity>>

    @Query("SELECT COUNT(*) FROM incident_logs")
    fun getTotalIncidentCount(): Flow<Long>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertIncident(incident: IncidentLogEntity): Long

    @Query("DELETE FROM incident_logs")
    suspend fun clearAll()
}
