package com.example.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "session_records")
data class SessionRecord(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val startTime: Long,
    val endTime: Long,
    val durationSeconds: Long,
    val catchesCount: Int,
    val spinsCount: Int,
    val itemsCount: Int,
    val stardustEarned: Int,
    val xpEarned: Int,
    val profileName: String
)

@Entity(tableName = "automation_logs")
data class AutomationLogEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val timestamp: Long,
    val logType: String,
    val message: String,
    val detail: String? = null
)

@Entity(tableName = "bot_profiles")
data class BotProfileEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val priorityMode: String,
    val cycleIntervalMs: Long,
    val throwPowerBoost: Float,
    val fastCatchEnabled: Boolean,
    val curveDirection: String,
    val isDefault: Boolean = false
)
