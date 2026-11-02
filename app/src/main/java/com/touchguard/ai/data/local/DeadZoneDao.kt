package com.touchguard.ai.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface DeadZoneDao {

    @Query("SELECT * FROM dead_zones ORDER BY id ASC")
    fun getAllFlow(): Flow<List<DeadZoneEntity>>

    @Query("SELECT * FROM dead_zones ORDER BY id ASC")
    suspend fun getAll(): List<DeadZoneEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: DeadZoneEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entities: List<DeadZoneEntity>)

    @Update
    suspend fun update(entity: DeadZoneEntity)

    @Delete
    suspend fun delete(entity: DeadZoneEntity)

    @Query("DELETE FROM dead_zones WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("UPDATE dead_zones SET incidentCount = incidentCount + 1 WHERE id = :id")
    suspend fun incrementIncident(id: Long)
}
