package com.example.model

data class TelemetryStats(
    val catchesCount: Int = 0,
    val encountersSeen: Int = 0,
    val spinsCount: Int = 0,
    val itemsCollected: Int = 0,
    val stardustGained: Int = 0,
    val xpGained: Int = 0,
    val ballsUsed: Int = 0,
    val fastCatchesCount: Int = 0,
    val shiniesFound: Int = 0,
    val uptimeSeconds: Long = 0L,
    val lastActionTimestamp: Long = System.currentTimeMillis()
) {
    val catchSuccessRate: Float
        get() = if (encountersSeen > 0) (catchesCount.toFloat() / encountersSeen * 100f).coerceIn(0f, 100f) else 0f

    val formattedUptime: String
        get() {
            val hours = uptimeSeconds / 3600
            val minutes = (uptimeSeconds % 3600) / 60
            val seconds = uptimeSeconds % 60
            return String.format("%02d:%02d:%02d", hours, minutes, seconds)
        }
}
