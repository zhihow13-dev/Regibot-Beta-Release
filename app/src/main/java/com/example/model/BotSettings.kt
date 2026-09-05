package com.example.model

data class BotSettings(
    val priorityMode: PriorityMode = PriorityMode.ALL_IN_ONE,
    val cycleIntervalMs: Long = 1200L,
    val resultsPerInference: Int = 3,
    val throwPowerBoost: Float = 1.05f, // 0.6f - 1.4f
    val throwStyle: ThrowStyle = ThrowStyle.SMOOTH_CURVEBALL,
    val fastCatchEnabled: Boolean = false, // disabled by default so ball makes contact with Pokémon
    val curveDirection: CurveDirection = CurveDirection.COUNTER_CLOCKWISE,
    val targetGrade: ThrowGrade = ThrowGrade.EXCELLENT,
    val ballType: BallType = BallType.AUTO,
    val berryType: BerryType = BerryType.NONE, // clean throw without accidental menu clicks
    val humanizeJitterMs: Int = 180,
    val tapAccuracyVariancePx: Int = 8,
    val autoTransferTrash: Boolean = false,
    val floatingHudEnabled: Boolean = false,
    val realGameIntegration: Boolean = true
)

