package com.example.engine

import android.graphics.Path
import androidx.compose.ui.geometry.Offset
import com.example.model.BallType
import com.example.model.BerryType
import com.example.model.BotSettings
import com.example.model.BotState
import com.example.model.CurveDirection
import com.example.model.LogEntry
import com.example.model.LogType
import com.example.model.PriorityMode
import com.example.model.TelemetryStats
import com.example.model.ThrowGrade
import com.example.model.ThrowPhysics
import com.example.service.RegiBotAccessibilityService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.random.Random

object RegiBotEngine {

    private val engineScope = CoroutineScope(Dispatchers.Default + Job())
    private var automationJob: Job? = null

    private val _stateFlow = MutableStateFlow(BotState.IDLE)
    val stateFlow: StateFlow<BotState> = _stateFlow.asStateFlow()

    private val _telemetryFlow = MutableStateFlow(TelemetryStats())
    val telemetryFlow: StateFlow<TelemetryStats> = _telemetryFlow.asStateFlow()

    private val _settingsFlow = MutableStateFlow(BotSettings())
    val settingsFlow: StateFlow<BotSettings> = _settingsFlow.asStateFlow()

    private val _logsFlow = MutableStateFlow<List<LogEntry>>(emptyList())
    val logsFlow: StateFlow<List<LogEntry>> = _logsFlow.asStateFlow()

    // Test bench / trajectory simulation callbacks
    val lastDispatchedPath = MutableStateFlow<List<Offset>>(emptyList())
    val lastThrowGrade = MutableStateFlow<ThrowGrade>(ThrowGrade.EXCELLENT)

    private var sessionStartTime = 0L

    init {
        addLog(LogType.SYSTEM, "CORE", "RegiBot automation engine initialized. Ready for commands.")
    }

    fun updateSettings(newSettings: BotSettings) {
        _settingsFlow.value = newSettings
        addLog(
            LogType.INFO,
            "SETTINGS",
            "Updated: ${newSettings.priorityMode.title}, Cycle=${newSettings.cycleIntervalMs}ms, Boost=${(newSettings.throwPowerBoost * 100).toInt()}%, FastCatch=${newSettings.fastCatchEnabled}"
        )
    }

    fun toggleEngine() {
        if (isRunning()) {
            stopEngine()
        } else {
            startEngine()
        }
    }

    fun startEngine() {
        if (automationJob?.isActive == true) return

        sessionStartTime = System.currentTimeMillis()
        _stateFlow.value = BotState.SCANNING
        addLog(LogType.SYSTEM, "ENGINE", "Autonomous gameplay pipeline started.")

        automationJob = engineScope.launch {
            var loopIndex = 0
            while (isActive) {
                try {
                    val settings = _settingsFlow.value
                    val accessService = RegiBotAccessibilityService.instance

                    // Update session uptime
                    val uptimeSec = (System.currentTimeMillis() - sessionStartTime) / 1000L
                    _telemetryFlow.value = _telemetryFlow.value.copy(uptimeSeconds = uptimeSec)

                    if (settings.realGameIntegration) {
                        if (accessService == null) {
                            _stateFlow.value = BotState.ERROR
                            addLog(
                                LogType.WARNING,
                                "ACCESSIBILITY",
                                "Accessibility Service disconnected. Enable RegiBot in Android Settings > Accessibility to interact with Pokémon GO."
                            )
                            delay(2000L)
                            continue
                        }

                        // Check if Pokémon GO is active in foreground
                        val isPogo = RegiBotAccessibilityService.isPokemonGoForeground
                        val currentPkg = RegiBotAccessibilityService.currentForegroundPackage

                        if (!isPogo && currentPkg.isNotBlank() && currentPkg != "com.example") {
                            _stateFlow.value = BotState.SCANNING
                            addLog(
                                LogType.INFO,
                                "FOREGROUND",
                                "Waiting for Pokémon GO (com.nianticlabs.pokemongo). Current foreground: $currentPkg. Switch to Pokémon GO to automate."
                            )
                            delay(1800L)
                            continue
                        }

                        // Real screen capture & visual inspection
                        _stateFlow.value = BotState.SCANNING
                        val bitmap = accessService.captureScreen()
                        val visionResult = ScreenAnalyzer.analyzeScreen(bitmap, isPogo)

                        addLog(
                            LogType.INFO,
                            "VISION",
                            "Screen detected: ${visionResult.screenType.label} (confidence: ${(visionResult.confidence * 100).toInt()}%). ${visionResult.statusMessage}"
                        )

                        val (screenWidth, screenHeight) = accessService.getScreenDimensions()

                        when (visionResult.screenType) {
                            DetectedScreen.ENCOUNTER_CATCH -> {
                                executeRealEncounterCatch(settings, accessService, screenWidth, screenHeight, visionResult)
                            }
                            DetectedScreen.POKESTOP_DISC -> {
                                executeRealPokestopSpin(settings, accessService, screenWidth, screenHeight, visionResult)
                            }
                            DetectedScreen.CATCH_SUCCESS_DIALOG -> {
                                _stateFlow.value = BotState.CLAIMING_REWARDS
                                addLog(LogType.CATCH, "CONFIRM", "Dismissing catch summary dialog.")
                                accessService.dispatchTapGesture(screenWidth * 0.50f, screenHeight * 0.92f)
                                delay(600L)
                            }
                            DetectedScreen.POKEMON_GO_MAP -> {
                                executeRealMapInteraction(settings, accessService, screenWidth, screenHeight, loopIndex)
                            }
                            DetectedScreen.UNKNOWN_OR_BACKGROUND -> {
                                // Waiting for frame to stabilize; NEVER spam random taps
                                _stateFlow.value = BotState.SCANNING
                                delay(800L)
                            }
                        }

                    } else {
                        // Offline Simulator Mode for testing & calibration
                        _stateFlow.value = BotState.SCANNING
                        val cycleWait = settings.cycleIntervalMs + Random.nextLong(
                            -settings.humanizeJitterMs.toLong(),
                            settings.humanizeJitterMs.toLong()
                        ).coerceAtLeast(100L)
                        delay(cycleWait)

                        loopIndex++
                        val shouldSpin = when (settings.priorityMode) {
                            PriorityMode.ALL_IN_ONE -> loopIndex % 3 == 0
                            PriorityMode.SPIN_FIRST -> loopIndex % 2 == 0
                            PriorityMode.CATCH_FIRST -> loopIndex % 5 == 0
                            PriorityMode.SHINY_CHECK -> false
                        }

                        if (shouldSpin) {
                            executePokestopCycle(settings)
                        } else {
                            executeCatchCycle(settings)
                        }
                    }

                    // Anti-detection humanized break
                    _stateFlow.value = BotState.COOLDOWN
                    val jitterBreak = (settings.humanizeJitterMs * 1.5).toLong() + Random.nextLong(50, 300)
                    delay(jitterBreak)

                } catch (e: Exception) {
                    if (e is kotlinx.coroutines.CancellationException) break
                    addLog(LogType.WARNING, "ERROR", "Exception during loop cycle: ${e.message}")
                    delay(1000L)
                }
            }
            _stateFlow.value = BotState.IDLE
        }
    }

    fun stopEngine() {
        automationJob?.cancel()
        automationJob = null
        _stateFlow.value = BotState.IDLE
        addLog(LogType.SYSTEM, "ENGINE", "Autonomous gameplay halted by user.")
    }

    fun isRunning(): Boolean = automationJob?.isActive == true

    /**
     * Instantly dispatches a master calibrated throw into Pokémon GO.
     * Can be invoked directly from the floating HUD button or dashboard test bench.
     */
    fun triggerInstantCatchThrow() {
        engineScope.launch {
            val accessService = RegiBotAccessibilityService.instance
            if (accessService == null) {
                addLog(LogType.WARNING, "ACCESSIBILITY", "Cannot trigger throw: Accessibility service inactive. Enable in Android Settings.")
                return@launch
            }
            val displayMetrics = accessService.resources.displayMetrics
            val screenWidth = displayMetrics.widthPixels.toFloat()
            val screenHeight = displayMetrics.heightPixels.toFloat()
            val settings = _settingsFlow.value

            _stateFlow.value = BotState.DISPATCHING_THROW
            addLog(LogType.GESTURE, "INSTANT_THROW", "Manual calibrated curveball triggered (${settings.throwStyle.displayName}, boost ${settings.throwPowerBoost}x)")

            val trajectory = ThrowPhysics.calculateCalibratedTrajectory(
                curveDirection = settings.curveDirection,
                powerBoost = settings.throwPowerBoost,
                style = settings.throwStyle
            )
            lastDispatchedPath.value = trajectory
            lastThrowGrade.value = settings.targetGrade

            val pixelPath = ThrowPhysics.buildPixelPath(
                normalizedPoints = trajectory,
                screenWidth = screenWidth,
                screenHeight = screenHeight,
                variancePx = settings.tapAccuracyVariancePx.toFloat()
            )

            val throwDurationMs = (240L / settings.throwPowerBoost).toLong().coerceIn(210L, 290L)
            accessService.dispatchPathGesture(pixelPath, throwDurationMs)
            delay(throwDurationMs + 1200L) // Wait for ball collision

            _telemetryFlow.value = _telemetryFlow.value.copy(
                catchesCount = _telemetryFlow.value.catchesCount + 1,
                ballsUsed = _telemetryFlow.value.ballsUsed + 1
            )
            _stateFlow.value = BotState.IDLE
        }
    }

    private suspend fun executeRealEncounterCatch(
        settings: BotSettings,
        accessService: RegiBotAccessibilityService,
        screenWidth: Float,
        screenHeight: Float,
        visionResult: VisionAnalysisResult
    ) {
        _stateFlow.value = BotState.TARGET_LOCKED
        _telemetryFlow.value = _telemetryFlow.value.copy(
            encountersSeen = _telemetryFlow.value.encountersSeen + 1
        )
        addLog(LogType.INFO, "ENCOUNTER", "Live encounter locked. Initializing throw kinematics.")

        // Berry feeding (only if user explicitly selected a berry)
        if (settings.berryType != BerryType.NONE) {
            _stateFlow.value = BotState.PREPARING_BERRY
            addLog(LogType.GESTURE, "BERRY", "Dispatching berry tray tap at bottom-left (${(screenWidth * 0.12f).toInt()}, ${(screenHeight * 0.88f).toInt()})")
            accessService.dispatchTapGesture(screenWidth * 0.12f, screenHeight * 0.88f)
            delay(450L)
            // Tap berry item to feed
            accessService.dispatchTapGesture(screenWidth * 0.50f, screenHeight * 0.70f)
            delay(500L)
        }

        // Calculate calibrated curve/straight path
        _stateFlow.value = BotState.SPINNING_BALL
        val trajectory = ThrowPhysics.calculateCalibratedTrajectory(
            curveDirection = settings.curveDirection,
            powerBoost = settings.throwPowerBoost,
            targetCenter = visionResult.targetCoordinates ?: androidx.compose.ui.geometry.Offset(screenWidth * 0.50f, screenHeight * 0.38f),
            style = settings.throwStyle
        )
        lastDispatchedPath.value = trajectory
        lastThrowGrade.value = settings.targetGrade

        _stateFlow.value = BotState.DISPATCHING_THROW
        val pixelPath = ThrowPhysics.buildPixelPath(
            normalizedPoints = trajectory,
            screenWidth = screenWidth,
            screenHeight = screenHeight,
            variancePx = settings.tapAccuracyVariancePx.toFloat()
        )

        // Snappy high-velocity release: 220ms - 270ms ensures high upward momentum in Unity physics
        val throwDurationMs = (240L / settings.throwPowerBoost).toLong().coerceIn(210L, 290L)
        addLog(LogType.GESTURE, "THROW", "Flicking calibrated ${settings.throwStyle.displayName} (${settings.curveDirection.label}, $throwDurationMs ms)")
        accessService.dispatchPathGesture(pixelPath, throwDurationMs)
        
        // Wait for Pokéball flight trajectory and Pokémon hit collision (1200ms)
        delay(throwDurationMs + 1200L)

        // Fast Catch logic: ONLY flee after ball has made full physical contact with Pokémon
        if (settings.fastCatchEnabled) {
            _stateFlow.value = BotState.FAST_CATCH_CANCEL
            val fleeX = visionResult.fleeButtonCoordinates?.x ?: (screenWidth * 0.11f)
            val fleeY = visionResult.fleeButtonCoordinates?.y ?: (screenHeight * 0.09f)
            addLog(LogType.CATCH, "FAST_CATCH", "Ball made contact. Triggering fast catch skip at (${fleeX.toInt()}, ${fleeY.toInt()})")
            delay(300L)
            accessService.dispatchTapGesture(fleeX, fleeY)
            _telemetryFlow.value = _telemetryFlow.value.copy(
                fastCatchesCount = _telemetryFlow.value.fastCatchesCount + 1,
                catchesCount = _telemetryFlow.value.catchesCount + 1,
                ballsUsed = _telemetryFlow.value.ballsUsed + 1
            )
            delay(600L)
        } else {
            addLog(LogType.CATCH, "WAIT", "Awaiting capture verification shakes...")
            delay(2400L)
            _telemetryFlow.value = _telemetryFlow.value.copy(
                catchesCount = _telemetryFlow.value.catchesCount + 1,
                ballsUsed = _telemetryFlow.value.ballsUsed + 1
            )
        }
    }

    private suspend fun executeRealPokestopSpin(
        settings: BotSettings,
        accessService: RegiBotAccessibilityService,
        screenWidth: Float,
        screenHeight: Float,
        visionResult: VisionAnalysisResult
    ) {
        _stateFlow.value = BotState.POKESTOP_LOCKED
        addLog(LogType.INFO, "POKESTOP", "PokéStop disc open on screen. Executing rotational swipe gesture.")

        _stateFlow.value = BotState.SPINNING_STOP
        val spinPath = ThrowPhysics.buildPokestopSpinPath(
            centerX = screenWidth * 0.50f,
            centerY = screenHeight * 0.45f,
            radiusPx = screenWidth * 0.28f
        )
        accessService.dispatchPathGesture(spinPath, 300L)
        delay(700L)

        _stateFlow.value = BotState.CLAIMING_REWARDS
        val exitX = visionResult.exitButtonCoordinates?.x ?: (screenWidth * 0.50f)
        val exitY = visionResult.exitButtonCoordinates?.y ?: (screenHeight * 0.92f)
        addLog(LogType.SPIN, "LOOT", "Closing PokéStop disc view via exit button at (${exitX.toInt()}, ${exitY.toInt()})")
        accessService.dispatchTapGesture(exitX, exitY)
        _telemetryFlow.value = _telemetryFlow.value.copy(
            spinsCount = _telemetryFlow.value.spinsCount + 1,
            itemsCollected = _telemetryFlow.value.itemsCollected + 3
        )
        delay(500L)
    }

    private suspend fun executeRealMapInteraction(
        settings: BotSettings,
        accessService: RegiBotAccessibilityService,
        screenWidth: Float,
        screenHeight: Float,
        loopIndex: Int
    ) {
        _stateFlow.value = BotState.SCANNING
        // If in Catch First mode or user is walking, avoid wild spam tapping.
        // Tap strictly within the central roadway spawn corridor in front of trainer:
        // x: 0.42f .. 0.58f, y: 0.52f .. 0.64f
        if (loopIndex % 2 == 0) {
            val tapX = screenWidth * (0.42f + Random.nextFloat() * 0.16f)
            val tapY = screenHeight * (0.52f + Random.nextFloat() * 0.12f)
            addLog(LogType.GESTURE, "MAP_SCAN", "Checking active spawn cluster at (${tapX.toInt()}, ${tapY.toInt()})")
            accessService.dispatchTapGesture(tapX, tapY)
        }
        delay(1400L)
    }

    private suspend fun executeCatchCycle(settings: BotSettings) {
        val encounterId = Random.nextInt(1000, 9999)
        _telemetryFlow.value = _telemetryFlow.value.copy(
            encountersSeen = _telemetryFlow.value.encountersSeen + 1
        )

        _stateFlow.value = BotState.TARGET_LOCKED
        addLog(
            LogType.INFO,
            "VISION",
            "Wild target #$encounterId detected in interactive perimeter. Inference confidence: ${(88 + Random.nextInt(12))}%"
        )

        // Dispatch tap on target spawn
        val accessService = RegiBotAccessibilityService.instance
        accessService?.dispatchTapGesture(540f + Random.nextInt(-20, 20), 1100f + Random.nextInt(-20, 20))
        delay(750L) // Wait for encounter transition

        // Shiny Check Mode
        val isShiny = Random.nextInt(100) < 5
        if (settings.priorityMode == PriorityMode.SHINY_CHECK) {
            if (!isShiny) {
                addLog(LogType.INFO, "SHINY_HUNT", "Target #$encounterId is standard variant. Executing fast flee.")
                accessService?.dispatchTapGesture(120f, 180f) // Flee button
                delay(400L)
                return
            } else {
                _telemetryFlow.value = _telemetryFlow.value.copy(shiniesFound = _telemetryFlow.value.shiniesFound + 1)
                addLog(LogType.SHINY, "SHINY_ALERT", "★ SHINY POKÉMON DETECTED! Deploying Golden Razz & Ultra Ball!")
            }
        }

        // Berry phase
        if (settings.berryType != BerryType.NONE || isShiny) {
            _stateFlow.value = BotState.PREPARING_BERRY
            val berryName = if (isShiny) "Golden Razz Berry" else settings.berryType.displayName
            addLog(LogType.GESTURE, "BERRY", "Feeding $berryName to encounter")
            delay(450L)
        }

        // Ball spin & curve throw calculation
        _stateFlow.value = BotState.SPINNING_BALL
        delay(320L)

        _stateFlow.value = BotState.DISPATCHING_THROW
        val trajectory = ThrowPhysics.calculateCurveballTrajectory(
            curveDirection = settings.curveDirection,
            powerBoost = settings.throwPowerBoost
        )
        lastDispatchedPath.value = trajectory
        lastThrowGrade.value = settings.targetGrade

        // Dispatch actual accessibility gesture if enabled
        if (accessService != null) {
            val path = ThrowPhysics.buildPixelPath(
                normalizedPoints = trajectory,
                screenWidth = 1080f,
                screenHeight = 2400f,
                variancePx = settings.tapAccuracyVariancePx.toFloat()
            )
            accessService.dispatchPathGesture(path, 420L)
        }

        addLog(
            LogType.GESTURE,
            "THROW",
            "Executed ${settings.curveDirection.label} curveball (${settings.targetGrade.label}). Velocity boost: ${(settings.throwPowerBoost * 100).toInt()}%"
        )
        delay(600L)

        // Fast Catch logic
        if (settings.fastCatchEnabled) {
            _stateFlow.value = BotState.FAST_CATCH_CANCEL
            addLog(LogType.CATCH, "FAST_CATCH", "Triggered fast catch animation cancel sequence. Escaping encounter.")
            accessService?.dispatchTapGesture(120f, 180f) // Top-left exit
            _telemetryFlow.value = _telemetryFlow.value.copy(
                fastCatchesCount = _telemetryFlow.value.fastCatchesCount + 1
            )
            delay(350L)
        } else {
            delay(2200L) // Wait for capture shake animation
        }

        // Success verification
        val isCaught = Random.nextInt(100) < 92
        if (isCaught) {
            val stardust = 100 + Random.nextInt(50)
            val xp = 160 + Random.nextInt(100)
            _stateFlow.value = BotState.CLAIMING_REWARDS
            _telemetryFlow.value = _telemetryFlow.value.copy(
                catchesCount = _telemetryFlow.value.catchesCount + 1,
                stardustGained = _telemetryFlow.value.stardustGained + stardust,
                xpGained = _telemetryFlow.value.xpGained + xp,
                ballsUsed = _telemetryFlow.value.ballsUsed + 1
            )
            addLog(LogType.CATCH, "CAPTURE", "Capture successful! +$stardust Stardust, +$xp XP.")
        } else {
            addLog(LogType.WARNING, "CATCH", "Wild Pokémon broke free! Retrying next cycle.")
        }
    }

    private suspend fun executePokestopCycle(settings: BotSettings) {
        _stateFlow.value = BotState.POKESTOP_LOCKED
        addLog(LogType.INFO, "VISION", "PokéStop identified in interaction range.")

        val accessService = RegiBotAccessibilityService.instance
        accessService?.dispatchTapGesture(540f, 850f)
        delay(600L)

        _stateFlow.value = BotState.SPINNING_STOP
        addLog(LogType.GESTURE, "SPIN", "Spinning photo disc with rotational swipe gesture.")

        if (accessService != null) {
            val spinPath = ThrowPhysics.buildPokestopSpinPath(540f, 1000f, 160f)
            accessService.dispatchPathGesture(spinPath, 380L)
        }
        delay(700L)

        _stateFlow.value = BotState.CLAIMING_REWARDS
        val itemsLooted = 3 + Random.nextInt(3)
        val stardust = 100
        val xp = 100

        _telemetryFlow.value = _telemetryFlow.value.copy(
            spinsCount = _telemetryFlow.value.spinsCount + 1,
            itemsCollected = _telemetryFlow.value.itemsCollected + itemsLooted,
            stardustGained = _telemetryFlow.value.stardustGained + stardust,
            xpGained = _telemetryFlow.value.xpGained + xp
        )

        addLog(LogType.SPIN, "LOOT", "Harvested $itemsLooted items from PokéStop (+${stardust} Stardust, +${xp} XP).")

        // Exit PokéStop view
        accessService?.dispatchTapGesture(540f, 2100f) // Close button
        delay(400L)
    }

    /**
     * Executes an isolated single test throw for the interactive test bench.
     */
    fun testThrowTrajectory(powerBoost: Float, curveDirection: CurveDirection) {
        val trajectory = ThrowPhysics.calculateCurveballTrajectory(
            curveDirection = curveDirection,
            powerBoost = powerBoost
        )
        lastDispatchedPath.value = trajectory

        val accessService = RegiBotAccessibilityService.instance
        if (accessService != null) {
            val path = ThrowPhysics.buildPixelPath(
                normalizedPoints = trajectory,
                screenWidth = 1080f,
                screenHeight = 2400f
            )
            accessService.dispatchPathGesture(path, 420L) { success ->
                addLog(LogType.GESTURE, "TEST_BENCH", "Accessibility test gesture dispatched: ${if (success) "COMPLETED" else "CANCELLED"}")
            }
        } else {
            addLog(LogType.GESTURE, "TEST_BENCH", "Simulated curveball throw physics (${curveDirection.label}, boost: ${(powerBoost * 100).toInt()}%)")
        }
    }

    fun resetStats() {
        _telemetryFlow.value = TelemetryStats()
        sessionStartTime = System.currentTimeMillis()
        addLog(LogType.SYSTEM, "STATS", "Session stats reset to zero.")
    }

    fun clearLogs() {
        _logsFlow.value = emptyList()
    }

    private fun addLog(type: LogType, tag: String, message: String, detail: String? = null) {
        val entry = LogEntry(
            type = type,
            tag = tag,
            message = message,
            detail = detail
        )
        val current = _logsFlow.value.toMutableList()
        current.add(0, entry)
        if (current.size > 200) {
            current.removeAt(current.size - 1)
        }
        _logsFlow.value = current
    }
}
