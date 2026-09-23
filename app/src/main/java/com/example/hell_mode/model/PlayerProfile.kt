package com.example.hell_mode.model

data class PlayerProfile(
    var level: Int = 1,
    var currentExp: Int = 0,
    var maxExp: Int = 100,
    var hp: Int = 100,
    var maxHp: Int = 100,
    var saldo: Long = 250, // Initial gold saldo (MMORPG)
    var realMoneySaldo: Long = 1000000L, // Real-life money saldo in Rupiah (IDR)
    var isHellMode: Boolean = false,
    var title: String = "Novice Adventurer",
    var totalQuestsCompleted: Int = 0,
    var totalStudyMinutes: Long = 0,
    var unlockedTitles: MutableList<String> = mutableListOf("Novice Adventurer")
) {
    fun addExpAndGold(expGained: Int, goldGained: Int): Boolean {
        var leveledUp = false
        currentExp += expGained
        saldo += goldGained
        
        while (currentExp >= maxExp) {
            currentExp -= maxExp
            level++
            maxExp = (maxExp * 1.25).toInt()
            hp = maxHp // Full heal on level up
            leveledUp = true
            updateTitle()
        }
        return leveledUp
    }

    private fun updateTitle() {
        title = when {
            isHellMode && level >= 20 -> "Hell Monarch"
            isHellMode && level >= 10 -> "Hell Walker"
            isHellMode -> "Demon Slayer Novice"
            level >= 25 -> "Grandmaster S-Rank"
            level >= 15 -> "Shadow Knight"
            level >= 10 -> "Senior Scholar"
            level >= 5 -> "Veteran Adventurer"
            else -> "Novice Adventurer"
        }
        if (!unlockedTitles.contains(title)) {
            unlockedTitles.add(title)
        }
    }

    fun applyHellMode(enabled: Boolean) {
        isHellMode = enabled
        updateTitle()
    }
}
