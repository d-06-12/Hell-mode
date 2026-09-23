package com.example.hell_mode.model

import java.util.UUID

data class Quest(
    val id: String = UUID.randomUUID().toString(),
    var title: String,
    var description: String,
    var category: String, // Olahraga, Belajar, Kebiasaan, Rutinitas, Boss Quest
    var specializationId: String? = null,
    var specializationName: String? = null,
    var difficulty: QuestDifficulty = QuestDifficulty.EASY,
    var expReward: Int = 20,
    var goldReward: Int = 10,
    var timerMinutesRequired: Int = 0,
    var dueDateMillis: Long = System.currentTimeMillis(),
    var isCompleted: Boolean = false,
    var isPenalized: Boolean = false, // True if overdue HP penalty has been applied
    var isHellExclusive: Boolean = false,
    var createdAt: Long = System.currentTimeMillis()
)
