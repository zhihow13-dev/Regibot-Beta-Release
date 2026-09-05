package com.example.viewmodel

import android.app.Application
import android.content.Context
import android.provider.Settings
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.BotRepository
import com.example.data.RegiBotDatabase
import com.example.data.entity.BotProfileEntity
import com.example.data.entity.SessionRecord
import com.example.engine.RegiBotEngine
import com.example.model.BallType
import com.example.model.BerryType
import com.example.model.BotSettings
import com.example.model.BotState
import com.example.model.CurveDirection
import com.example.model.LogEntry
import com.example.model.PriorityMode
import com.example.model.TelemetryStats
import com.example.model.ThrowGrade
import com.example.service.RegiBotAccessibilityService
import com.example.service.RegiBotOverlayService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class RegiBotViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: BotRepository
    val botState: StateFlow<BotState> = RegiBotEngine.stateFlow
    val telemetry: StateFlow<TelemetryStats> = RegiBotEngine.telemetryFlow
    val settings: StateFlow<BotSettings> = RegiBotEngine.settingsFlow
    val logs: StateFlow<List<LogEntry>> = RegiBotEngine.logsFlow

    private val _isAccessibilityConnected = MutableStateFlow(false)
    val isAccessibilityConnected: StateFlow<Boolean> = _isAccessibilityConnected.asStateFlow()

    private val _canDrawOverlays = MutableStateFlow(false)
    val canDrawOverlays: StateFlow<Boolean> = _canDrawOverlays.asStateFlow()

    val savedSessions: StateFlow<List<SessionRecord>>
    val savedProfiles: StateFlow<List<BotProfileEntity>>

    init {
        val db = RegiBotDatabase.getDatabase(application)
        repository = BotRepository(db.botDao())

        savedSessions = repository.allSessions.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

        savedProfiles = repository.allProfiles.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

        viewModelScope.launch {
            repository.seedDefaultProfilesIfEmpty()
        }

        refreshPermissions()
    }

    fun refreshPermissions() {
        val context = getApplication<Application>()
        _isAccessibilityConnected.value =
            RegiBotAccessibilityService.isServiceActive ||
                    RegiBotAccessibilityService.isAccessibilitySettingsEnabled(context)
        _canDrawOverlays.value = Settings.canDrawOverlays(context)
    }

    fun toggleEngine() {
        RegiBotEngine.toggleEngine()
    }

    fun startEngine() {
        RegiBotEngine.startEngine()
    }

    fun stopEngine() {
        RegiBotEngine.stopEngine()
    }

    fun updateSettings(newSettings: BotSettings) {
        RegiBotEngine.updateSettings(newSettings)
    }

    fun setPriorityMode(mode: PriorityMode) {
        updateSettings(settings.value.copy(priorityMode = mode))
    }

    fun setCycleInterval(intervalMs: Long) {
        updateSettings(settings.value.copy(cycleIntervalMs = intervalMs))
    }

    fun setThrowPowerBoost(boost: Float) {
        updateSettings(settings.value.copy(throwPowerBoost = boost))
    }

    fun setFastCatchEnabled(enabled: Boolean) {
        updateSettings(settings.value.copy(fastCatchEnabled = enabled))
    }

    fun setCurveDirection(direction: CurveDirection) {
        updateSettings(settings.value.copy(curveDirection = direction))
    }

    fun setTargetGrade(grade: ThrowGrade) {
        updateSettings(settings.value.copy(targetGrade = grade))
    }

    fun setBallType(ball: BallType) {
        updateSettings(settings.value.copy(ballType = ball))
    }

    fun setBerryType(berry: BerryType) {
        updateSettings(settings.value.copy(berryType = berry))
    }

    fun setJitter(jitterMs: Int) {
        updateSettings(settings.value.copy(humanizeJitterMs = jitterMs))
    }

    fun setAccuracyVariance(variancePx: Int) {
        updateSettings(settings.value.copy(tapAccuracyVariancePx = variancePx))
    }

    fun toggleFloatingHud(enabled: Boolean, context: Context) {
        updateSettings(settings.value.copy(floatingHudEnabled = enabled))
        if (enabled && Settings.canDrawOverlays(context)) {
            RegiBotOverlayService.startOverlay(context)
        } else {
            RegiBotOverlayService.stopOverlay(context)
        }
    }

    fun testThrow(powerBoost: Float, direction: CurveDirection) {
        RegiBotEngine.testThrowTrajectory(powerBoost, direction)
    }

    fun setThrowStyle(style: com.example.model.ThrowStyle) {
        updateSettings(settings.value.copy(throwStyle = style))
    }

    fun triggerInstantCatchThrow() {
        RegiBotEngine.triggerInstantCatchThrow()
    }

    fun saveCurrentSession() {
        val stats = telemetry.value
        if (stats.catchesCount == 0 && stats.spinsCount == 0 && stats.uptimeSeconds < 5) return

        viewModelScope.launch {
            repository.saveSession(
                SessionRecord(
                    startTime = System.currentTimeMillis() - (stats.uptimeSeconds * 1000L),
                    endTime = System.currentTimeMillis(),
                    durationSeconds = stats.uptimeSeconds,
                    catchesCount = stats.catchesCount,
                    spinsCount = stats.spinsCount,
                    itemsCount = stats.itemsCollected,
                    stardustEarned = stats.stardustGained,
                    xpEarned = stats.xpGained,
                    profileName = settings.value.priorityMode.title
                )
            )
            RegiBotEngine.resetStats()
        }
    }

    fun resetStats() {
        RegiBotEngine.resetStats()
    }

    fun clearLogs() {
        RegiBotEngine.clearLogs()
        viewModelScope.launch {
            repository.clearLogs()
        }
    }
}
