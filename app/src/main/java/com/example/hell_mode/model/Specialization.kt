package com.example.hell_mode.model

import java.util.UUID

data class Specialization(
    val id: String = UUID.randomUUID().toString(),
    var name: String,
    var category: String = "Akademik", // Akademik, Skill, Fisik, Kreatif
    var level: Int = 1,
    var currentExp: Int = 0,
    var maxExp: Int = 100,
    var totalMinutesStudied: Long = 0,
    var colorHex: String = "#3498DB",
    var iconName: String = "book"
) {
    fun addStudyTimeAndExp(minutes: Long, expGained: Int): Boolean {
        totalMinutesStudied += minutes
        currentExp += expGained
        var leveledUp = false
        while (currentExp >= maxExp) {
            currentExp -= maxExp
            level++
            maxExp = (maxExp * 1.3).toInt()
            leveledUp = true
        }
        return leveledUp
    }
}
