package com.example.hell_mode.model

enum class QuestDifficulty(
    val displayName: String,
    val baseExp: Int,
    val baseGold: Int,
    val hpPenalty: Int
) {
    EASY("Easy (E)", 20, 10, 5),
    MEDIUM("Medium (C)", 50, 25, 15),
    HARD("Hard (A)", 100, 50, 30),
    HELL("HELL (S)", 250, 120, 50);

    companion object {
        fun fromString(name: String): QuestDifficulty {
            return entries.find { it.name.equals(name, ignoreCase = true) } ?: EASY
        }
    }
}
