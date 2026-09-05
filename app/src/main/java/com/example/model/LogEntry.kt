package com.example.model

enum class LogType {
    INFO,
    CATCH,
    SPIN,
    GESTURE,
    SHINY,
    WARNING,
    SYSTEM
}

data class LogEntry(
    val id: String = java.util.UUID.randomUUID().toString(),
    val timestamp: Long = System.currentTimeMillis(),
    val type: LogType = LogType.INFO,
    val tag: String = "ENGINE",
    val message: String,
    val detail: String? = null
) {
    val formattedTime: String
        get() {
            val sdf = java.text.SimpleDateFormat("HH:mm:ss.SSS", java.util.Locale.getDefault())
            return sdf.format(java.util.Date(timestamp))
        }
}
