package com.example.model

enum class BotState(
    val displayName: String,
    val description: String,
    val isActivelyExecuting: Boolean = true
) {
    IDLE("Idle", "Bot is ready to start automation", false),
    SCANNING("Scanning Screen", "Computer vision pipeline analyzing active frame", true),
    TARGET_LOCKED("Target Locked", "Wild Pokémon identified in interaction radius", true),
    POKESTOP_LOCKED("PokéStop In Range", "Photo disc detected and ready for spin", true),
    PREPARING_BERRY("Feeding Berry", "Applying configured berry to target", true),
    SPINNING_BALL("Spinning Curveball", "Calculating aerodynamic throw trajectory", true),
    DISPATCHING_THROW("Throwing Ball", "Accessibility gesture dispatched to screen", true),
    FAST_CATCH_CANCEL("Fast Catch Cancel", "Executing quick exit tap sequence", true),
    SPINNING_STOP("Spinning Disc", "Executing circular spin gesture on PokéStop", true),
    CLAIMING_REWARDS("Harvesting Loot", "Collecting items and field research tasks", true),
    COOLDOWN("Anti-Detection Pause", "Randomized humanized jitter interval", false),
    PAUSED("Paused", "Automation temporarily halted by user", false),
    ERROR("Configuration Required", "Accessibility permission or overlay needed", false)
}
