package com.example.model

enum class PriorityMode(
    val title: String,
    val description: String,
    val iconName: String
) {
    ALL_IN_ONE(
        title = "Auto Catch & Spin",
        description = "Balanced autonomous loop catching wild spawns and spinning discs",
        iconName = "auto_mode"
    ),
    CATCH_FIRST(
        title = "Wild Spawns First",
        description = "Prioritizes capturing Pokémon before interacting with nearby stops",
        iconName = "catching_pokemon"
    ),
    SPIN_FIRST(
        title = "PokéStop Priority",
        description = "Focuses on spinning photo discs to replenish bag inventory",
        iconName = "autorenew"
    ),
    SHINY_CHECK(
        title = "Shiny Hunter Only",
        description = "Enters encounter, detects shiny sparkle, flees if standard variant",
        iconName = "auto_awesome"
    )
}

enum class CurveDirection(val label: String) {
    COUNTER_CLOCKWISE("Left Spin (Anti-Clockwise)"),
    CLOCKWISE("Right Spin (Clockwise)"),
    STRAIGHT("Straight Precision")
}

enum class ThrowGrade(val label: String, val multiplier: Float, val targetRadiusFactor: Float) {
    NICE("Nice Throw", 1.15f, 0.75f),
    GREAT("Great Throw", 1.50f, 0.50f),
    EXCELLENT("Excellent Throw", 1.95f, 0.28f)
}

enum class BallType(val displayName: String, val tier: Int) {
    AUTO("Smart Selector (Auto)", 0),
    POKE_BALL("Standard Poké Ball", 1),
    GREAT_BALL("Great Ball (Blue)", 2),
    ULTRA_BALL("Ultra Ball (Yellow)", 3)
}

enum class BerryType(val displayName: String) {
    NONE("No Berry"),
    AUTO_PINAP("Auto Pinap (Extra Candy)"),
    RAZZ_BERRY("Razz Berry (Higher Catch Rate)"),
    GOLDEN_RAZZ("Golden Razz Berry (Max Catch Rate)"),
    SILVER_PINAP("Silver Pinap (Catch + Candy)")
}
