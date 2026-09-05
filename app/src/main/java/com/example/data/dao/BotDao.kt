package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.entity.AutomationLogEntity
import com.example.data.entity.BotProfileEntity
import com.example.data.entity.SessionRecord
import kotlinx.coroutines.flow.Flow

@Dao
interface BotDao {

    // Session Records
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: SessionRecord): Long

    @Query("SELECT * FROM session_records ORDER BY startTime DESC")
    fun getAllSessions(): Flow<List<SessionRecord>>

    @Query("SELECT * FROM session_records ORDER BY startTime DESC LIMIT 1")
    fun getLatestSession(): Flow<SessionRecord?>

    @Query("DELETE FROM session_records")
    suspend fun clearAllSessions()

    // Logs
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: AutomationLogEntity): Long

    @Query("SELECT * FROM automation_logs ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentLogs(limit: Int = 100): Flow<List<AutomationLogEntity>>

    @Query("DELETE FROM automation_logs")
    suspend fun clearLogs()

    // Profiles
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProfile(profile: BotProfileEntity): Long

    @Query("SELECT * FROM bot_profiles ORDER BY id ASC")
    fun getAllProfiles(): Flow<List<BotProfileEntity>>

    @Query("SELECT COUNT(*) FROM bot_profiles")
    suspend fun getProfileCount(): Int
}
