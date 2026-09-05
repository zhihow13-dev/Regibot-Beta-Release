package com.example.data

import com.example.data.dao.BotDao
import com.example.data.entity.AutomationLogEntity
import com.example.data.entity.BotProfileEntity
import com.example.data.entity.SessionRecord
import kotlinx.coroutines.flow.Flow

class BotRepository(private val botDao: BotDao) {

    val allSessions: Flow<List<SessionRecord>> = botDao.getAllSessions()
    val allProfiles: Flow<List<BotProfileEntity>> = botDao.getAllProfiles()
    val recentLogs: Flow<List<AutomationLogEntity>> = botDao.getRecentLogs(100)

    suspend fun saveSession(session: SessionRecord): Long = botDao.insertSession(session)

    suspend fun clearSessions() = botDao.clearAllSessions()

    suspend fun recordLog(type: String, message: String, detail: String? = null) {
        botDao.insertLog(
            AutomationLogEntity(
                timestamp = System.currentTimeMillis(),
                logType = type,
                message = message,
                detail = detail
            )
        )
    }

    suspend fun clearLogs() = botDao.clearLogs()

    suspend fun seedDefaultProfilesIfEmpty() {
        if (botDao.getProfileCount() == 0) {
            val defaults = listOf(
                BotProfileEntity(
                    name = "Balanced All-in-One",
                    priorityMode = "ALL_IN_ONE",
                    cycleIntervalMs = 1200L,
                    throwPowerBoost = 1.0f,
                    fastCatchEnabled = true,
                    curveDirection = "COUNTER_CLOCKWISE",
                    isDefault = true
                ),
                BotProfileEntity(
                    name = "Rapid Shiny Hunter",
                    priorityMode = "SHINY_CHECK",
                    cycleIntervalMs = 800L,
                    throwPowerBoost = 1.1f,
                    fastCatchEnabled = true,
                    curveDirection = "COUNTER_CLOCKWISE",
                    isDefault = false
                ),
                BotProfileEntity(
                    name = "PokéStop Hopper",
                    priorityMode = "SPIN_FIRST",
                    cycleIntervalMs = 1500L,
                    throwPowerBoost = 0.9f,
                    fastCatchEnabled = false,
                    curveDirection = "STRAIGHT",
                    isDefault = false
                )
            )
            defaults.forEach { botDao.insertProfile(it) }
        }
    }
}
