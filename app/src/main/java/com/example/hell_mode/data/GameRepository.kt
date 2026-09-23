package com.example.hell_mode.data

import android.content.Context
import android.content.SharedPreferences
import com.example.hell_mode.model.FinancialBook
import com.example.hell_mode.model.PlayerProfile
import com.example.hell_mode.model.Quest
import com.example.hell_mode.model.QuestDifficulty
import com.example.hell_mode.model.RealMoneyTransaction
import com.example.hell_mode.model.ShopItem
import com.example.hell_mode.model.Specialization
import com.example.hell_mode.model.TransactionType
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.Calendar

class GameRepository(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("hell_mode_game_prefs", Context.MODE_PRIVATE)
    private val gson = Gson()

    companion object {
        private const val KEY_PLAYER_PROFILE = "key_player_profile"
        private const val KEY_SPECIALIZATIONS = "key_specializations"
        private const val KEY_QUESTS = "key_quests"
        private const val KEY_SHOP_ITEMS = "key_shop_items"
        private const val KEY_TRANSACTIONS = "key_transactions"
        private const val KEY_FINANCIAL_BOOKS = "key_financial_books"
    }

    // --- PLAYER PROFILE ---
    fun getPlayerProfile(): PlayerProfile {
        val json = prefs.getString(KEY_PLAYER_PROFILE, null)
        val profile = if (json != null) {
            try {
                gson.fromJson(json, PlayerProfile::class.java)
            } catch (_: Exception) {
                PlayerProfile()
            }
        } else {
            PlayerProfile()
        }
        // Sync real-time net balance from accounting ledger (Net Balance = Total Income - Total Expense)
        profile.realMoneySaldo = getRealTimeNetBalance(null)
        return profile
    }

    fun savePlayerProfile(profile: PlayerProfile) {
        prefs.edit().putString(KEY_PLAYER_PROFILE, gson.toJson(profile)).apply()
    }

    fun setHellMode(enabled: Boolean): PlayerProfile {
        val profile = getPlayerProfile()
        profile.applyHellMode(enabled)
        savePlayerProfile(profile)
        return profile
    }

    // --- FINANCIAL BOOKS (BUKU KEUANGAN) ---
    fun getFinancialBooks(): List<FinancialBook> {
        val json = prefs.getString(KEY_FINANCIAL_BOOKS, null)
        if (json != null) {
            try {
                val type = object : TypeToken<List<FinancialBook>>() {}.type
                val list: List<FinancialBook> = gson.fromJson(json, type)
                if (list.isNotEmpty()) return list
            } catch (_: Exception) {}
        }
        val defaults = createDefaultFinancialBooks()
        saveFinancialBooks(defaults)
        return defaults
    }

    fun saveFinancialBooks(list: List<FinancialBook>) {
        prefs.edit().putString(KEY_FINANCIAL_BOOKS, gson.toJson(list)).apply()
    }

    fun addFinancialBook(book: FinancialBook) {
        val list = getFinancialBooks().toMutableList()
        list.add(book)
        saveFinancialBooks(list)
    }

    private fun createDefaultFinancialBooks(): List<FinancialBook> {
        return listOf(
            FinancialBook(
                id = "default_book",
                name = "Buku Utama / Kas",
                description = "Buku catatan keuangan harian pribadi",
                colorHex = "#FFC107"
            ),
            FinancialBook(
                id = "bank_book",
                name = "Dompet Digital & Bank",
                description = "Catatan rekening bank & e-wallet",
                colorHex = "#3498DB"
            ),
            FinancialBook(
                id = "business_book",
                name = "Buku Usaha & Bisnis",
                description = "Catatan keuangan proyek & bisnis",
                colorHex = "#2ECC71"
            )
        )
    }

    // --- SPECIALIZATIONS ---
    fun getSpecializations(): List<Specialization> {
        val json = prefs.getString(KEY_SPECIALIZATIONS, null)
        if (json != null) {
            try {
                val type = object : TypeToken<List<Specialization>>() {}.type
                val list: List<Specialization> = gson.fromJson(json, type)
                if (list.isNotEmpty()) return list
            } catch (_: Exception) {}
        }
        val defaults = createDefaultSpecializations()
        saveSpecializations(defaults)
        return defaults
    }

    fun saveSpecializations(list: List<Specialization>) {
        prefs.edit().putString(KEY_SPECIALIZATIONS, gson.toJson(list)).apply()
    }

    fun addSpecialization(specialization: Specialization) {
        val list = getSpecializations().toMutableList()
        list.add(specialization)
        saveSpecializations(list)
    }

    fun updateSpecialization(updatedSpec: Specialization) {
        val list = getSpecializations().toMutableList()
        val index = list.indexOfFirst { it.id == updatedSpec.id }
        if (index != -1) {
            list[index] = updatedSpec
            saveSpecializations(list)
        }
    }

    fun deleteSpecialization(specId: String) {
        val list = getSpecializations().filterNot { it.id == specId }
        saveSpecializations(list)
    }

    fun addExpToSpecialization(specId: String, studyMinutes: Long, baseExp: Int): Boolean {
        val list = getSpecializations().toMutableList()
        val index = list.indexOfFirst { it.id == specId }
        if (index == -1) return false

        val profile = getPlayerProfile()
        val multiplier = if (profile.isHellMode) 1.5 else 1.0
        val finalExp = (baseExp * multiplier).toInt()
        val finalGold = (studyMinutes * 2 * multiplier).toInt()

        val spec = list[index]
        val specLeveledUp = spec.addStudyTimeAndExp(studyMinutes, finalExp)
        list[index] = spec
        saveSpecializations(list)

        // Award player EXP & Gold
        val playerLeveledUp = profile.addExpAndGold(finalExp, finalGold)
        profile.totalStudyMinutes += studyMinutes
        savePlayerProfile(profile)

        return specLeveledUp || playerLeveledUp
    }

    private fun createDefaultSpecializations(): List<Specialization> {
        return listOf(
            Specialization(
                name = "Matematika & Logika",
                category = "Akademik",
                colorHex = "#3498DB",
                iconName = "calculator"
            ),
            Specialization(
                name = "Coding & Software",
                category = "Skill",
                colorHex = "#2ECC71",
                iconName = "code"
            ),
            Specialization(
                name = "Bahasa Inggris",
                category = "Akademik",
                colorHex = "#E67E22",
                iconName = "language"
            ),
            Specialization(
                name = "Olahraga & Fitness",
                category = "Fisik",
                colorHex = "#E74C3C",
                iconName = "fitness"
            ),
            Specialization(
                name = "Sains & Teknologi",
                category = "Akademik",
                colorHex = "#9B59B6",
                iconName = "science"
            )
        )
    }

    // --- QUESTS & OVERDUE PENALTIES ---
    fun getQuests(): List<Quest> {
        val json = prefs.getString(KEY_QUESTS, null)
        if (json != null) {
            try {
                val type = object : TypeToken<List<Quest>>() {}.type
                val list: List<Quest> = gson.fromJson(json, type)
                if (list.isNotEmpty()) return list
            } catch (_: Exception) {}
        }
        val specs = getSpecializations()
        val mathSpec = specs.find { it.name.contains("Matematika") }
        val codeSpec = specs.find { it.name.contains("Coding") }
        val fitnessSpec = specs.find { it.name.contains("Olahraga") }

        val defaults = createDefaultQuests(mathSpec, codeSpec, fitnessSpec)
        saveQuests(defaults)
        return defaults
    }

    fun saveQuests(list: List<Quest>) {
        prefs.edit().putString(KEY_QUESTS, gson.toJson(list)).apply()
    }

    fun addQuest(quest: Quest) {
        val list = getQuests().toMutableList()
        list.add(0, quest)
        saveQuests(list)
    }

    fun updateQuest(quest: Quest) {
        val list = getQuests().toMutableList()
        val idx = list.indexOfFirst { it.id == quest.id }
        if (idx != -1) {
            list[idx] = quest
            saveQuests(list)
        }
    }

    fun deleteQuest(questId: String) {
        val list = getQuests().filterNot { it.id == questId }
        saveQuests(list)
    }

    fun completeQuest(questId: String): Pair<Boolean, String> {
        val list = getQuests().toMutableList()
        val index = list.indexOfFirst { it.id == questId }
        if (index == -1) return Pair(false, "Quest tidak ditemukan")

        val quest = list[index]
        if (quest.isCompleted) return Pair(false, "Quest sudah selesai")

        quest.isCompleted = true
        list[index] = quest
        saveQuests(list)

        val profile = getPlayerProfile()
        val multiplier = if (profile.isHellMode) 1.5 else 1.0

        val finalExp = (quest.expReward * multiplier).toInt()
        val finalGold = (quest.goldReward * multiplier).toInt()

        val leveledUp = profile.addExpAndGold(finalExp, finalGold)
        profile.totalQuestsCompleted++
        savePlayerProfile(profile)

        // Also add EXP to linked specialization if present
        quest.specializationId?.let { specId ->
            addExpToSpecialization(specId, quest.timerMinutesRequired.toLong(), finalExp / 2)
        }

        val message = if (leveledUp) {
            "LEVEL UP! Mendapatkan +$finalExp EXP dan +$finalGold Gold Saldo!"
        } else {
            "Sukses! +$finalExp EXP, +$finalGold Gold Saldo!"
        }

        return Pair(leveledUp, message)
    }

    fun checkAndApplyOverdueQuestPenalties(): Pair<Int, List<String>> {
        val list = getQuests().toMutableList()
        val profile = getPlayerProfile()

        val calStartToday = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        var totalHpLost = 0
        val penalizedTitles = mutableListOf<String>()
        var listModified = false

        for (i in list.indices) {
            val quest = list[i]
            if (!quest.isCompleted && !quest.isPenalized && quest.dueDateMillis < calStartToday) {
                val penalty = if (profile.isHellMode) quest.difficulty.hpPenalty * 2 else quest.difficulty.hpPenalty
                totalHpLost += penalty
                penalizedTitles.add("${quest.title} (-$penalty HP)")

                quest.isPenalized = true
                list[i] = quest
                listModified = true
            }
        }

        if (totalHpLost > 0) {
            profile.hp = (profile.hp - totalHpLost).coerceAtLeast(0)
            savePlayerProfile(profile)
        }

        if (listModified) {
            saveQuests(list)
        }

        return Pair(totalHpLost, penalizedTitles)
    }

    private fun createDefaultQuests(
        mathSpec: Specialization?,
        codeSpec: Specialization?,
        fitnessSpec: Specialization?
    ): List<Quest> {
        val today = Calendar.getInstance().timeInMillis
        return listOf(
            Quest(
                title = "Workout Dumbbell & Cardio 20 Mnt",
                description = "Lakukan olahraga ringan untuk tingkatkan stamina fisik.",
                category = "Olahraga",
                specializationId = fitnessSpec?.id,
                specializationName = fitnessSpec?.name ?: "Olahraga",
                difficulty = QuestDifficulty.MEDIUM,
                expReward = 50,
                goldReward = 25,
                timerMinutesRequired = 20,
                dueDateMillis = today
            ),
            Quest(
                title = "Belajar Matematika & Algebra 30 Mnt",
                description = "Kerjakan soal latihan aljabar dan rumus dasar.",
                category = "Belajar",
                specializationId = mathSpec?.id,
                specializationName = mathSpec?.name ?: "Matematika",
                difficulty = QuestDifficulty.MEDIUM,
                expReward = 60,
                goldReward = 30,
                timerMinutesRequired = 30,
                dueDateMillis = today
            ),
            Quest(
                title = "Push Code & Refactor Project",
                description = "Selesaikan module Android Studio dan commit ke GitHub.",
                category = "Belajar",
                specializationId = codeSpec?.id,
                specializationName = codeSpec?.name ?: "Coding",
                difficulty = QuestDifficulty.HARD,
                expReward = 100,
                goldReward = 50,
                timerMinutesRequired = 45,
                dueDateMillis = today
            ),
            Quest(
                title = "Minum 2 Liter Air Putih",
                description = "Jaga hidrasi tubuh sepanjang hari.",
                category = "Kebiasaan",
                difficulty = QuestDifficulty.EASY,
                expReward = 20,
                goldReward = 10,
                timerMinutesRequired = 0,
                dueDateMillis = today
            ),
            Quest(
                title = "HELL BOSS: Selesaikan Focus Study 60 Mnt",
                description = "Tantangan Mode Hell! Fokus tanpa gangguan sama sekali.",
                category = "Boss Quest",
                difficulty = QuestDifficulty.HELL,
                expReward = 250,
                goldReward = 150,
                timerMinutesRequired = 60,
                dueDateMillis = today,
                isHellExclusive = true
            )
        )
    }

    // --- ACCOUNTING LEDGER & REAL-TIME FINANCIAL BALANCE CALCULATIONS ---
    fun getRealMoneyTransactions(bookId: String? = null): List<RealMoneyTransaction> {
        val json = prefs.getString(KEY_TRANSACTIONS, null)
        var list: List<RealMoneyTransaction> = emptyList()
        if (json != null) {
            try {
                val type = object : TypeToken<List<RealMoneyTransaction>>() {}.type
                list = gson.fromJson(json, type)
            } catch (_: Exception) {}
        }
        if (list.isEmpty()) {
            val defaults = listOf(
                RealMoneyTransaction(
                    title = "Gaji / Saku Bulanan",
                    amount = 2500000,
                    type = TransactionType.INCOME,
                    category = "Gaji/Saku",
                    bookId = "default_book",
                    bookName = "Buku Utama / Kas"
                ),
                RealMoneyTransaction(
                    title = "Makan Siang & Kopi",
                    amount = 35000,
                    type = TransactionType.EXPENSE,
                    category = "Makanan & Minuman",
                    bookId = "default_book",
                    bookName = "Buku Utama / Kas"
                )
            )
            saveRealMoneyTransactions(defaults)
            list = defaults
        }

        if (bookId != null && bookId != "all") {
            list = list.filter { it.bookId == bookId }
        }

        return list.sortedByDescending { it.timestamp }
    }

    fun saveRealMoneyTransactions(list: List<RealMoneyTransaction>) {
        prefs.edit().putString(KEY_TRANSACTIONS, gson.toJson(list)).apply()
    }

    fun addRealMoneyTransaction(transaction: RealMoneyTransaction) {
        val json = prefs.getString(KEY_TRANSACTIONS, null)
        val list: MutableList<RealMoneyTransaction> = if (json != null) {
            try {
                val type = object : TypeToken<List<RealMoneyTransaction>>() {}.type
                gson.fromJson(json, type)
            } catch (_: Exception) {
                mutableListOf()
            }
        } else {
            mutableListOf()
        }
        list.add(0, transaction)
        saveRealMoneyTransactions(list)
    }

    fun updateRealMoneyTransaction(transaction: RealMoneyTransaction) {
        val json = prefs.getString(KEY_TRANSACTIONS, null)
        if (json != null) {
            try {
                val type = object : TypeToken<List<RealMoneyTransaction>>() {}.type
                val list: MutableList<RealMoneyTransaction> = gson.fromJson(json, type)
                val idx = list.indexOfFirst { it.id == transaction.id }
                if (idx != -1) {
                    list[idx] = transaction
                    saveRealMoneyTransactions(list)
                }
            } catch (_: Exception) {}
        }
    }

    fun deleteRealMoneyTransaction(transId: String) {
        val json = prefs.getString(KEY_TRANSACTIONS, null)
        if (json != null) {
            try {
                val type = object : TypeToken<List<RealMoneyTransaction>>() {}.type
                val list: MutableList<RealMoneyTransaction> = gson.fromJson(json, type)
                list.removeAll { it.id == transId }
                saveRealMoneyTransactions(list)
            } catch (_: Exception) {}
        }
    }

    /**
     * Strict Accounting Ledger Real-time Calculations:
     * Saldo Akhir = Total Pemasukan - Total Pengeluaran
     */
    fun getTotalIncome(bookId: String? = null): Long {
        return getRealMoneyTransactions(bookId)
            .filter { it.type == TransactionType.INCOME }
            .sumOf { it.amount }
    }

    fun getTotalExpense(bookId: String? = null): Long {
        return getRealMoneyTransactions(bookId)
            .filter { it.type == TransactionType.EXPENSE }
            .sumOf { it.amount }
    }

    fun getRealTimeNetBalance(bookId: String? = null): Long {
        return getTotalIncome(bookId) - getTotalExpense(bookId)
    }

    // --- SHOP ITEMS ---
    fun getShopItems(): List<ShopItem> {
        val json = prefs.getString(KEY_SHOP_ITEMS, null)
        if (json != null) {
            try {
                val type = object : TypeToken<List<ShopItem>>() {}.type
                val list: List<ShopItem> = gson.fromJson(json, type)
                if (list.isNotEmpty()) return list
            } catch (_: Exception) {}
        }
        val defaults = createDefaultShopItems()
        saveShopItems(defaults)
        return defaults
    }

    fun saveShopItems(list: List<ShopItem>) {
        prefs.edit().putString(KEY_SHOP_ITEMS, gson.toJson(list)).apply()
    }

    fun addShopItem(item: ShopItem) {
        val list = getShopItems().toMutableList()
        list.add(item)
        saveShopItems(list)
    }

    fun buyShopItem(itemId: String): Pair<Boolean, String> {
        val items = getShopItems()
        val item = items.find { it.id == itemId } ?: return Pair(false, "Barang tidak ditemukan")

        val profile = getPlayerProfile()
        if (profile.saldo < item.cost) {
            return Pair(false, "Saldo Gold tidak cukup! Butuh ${item.cost} Gold.")
        }

        profile.saldo -= item.cost

        if (item.hpRestoreAmount > 0) {
            profile.hp = (profile.hp + item.hpRestoreAmount).coerceAtMost(profile.maxHp)
        }

        item.titleReward?.let { newTitle ->
            if (!profile.unlockedTitles.contains(newTitle)) {
                profile.unlockedTitles.add(newTitle)
            }
            profile.title = newTitle
        }

        savePlayerProfile(profile)
        return Pair(true, "Berhasil ditukar! ${item.title}")
    }

    private fun createDefaultShopItems(): List<ShopItem> {
        return listOf(
            ShopItem(
                title = "Main Game / Nonton Film 1 Jam",
                description = "Hadiah waktu bersantai tanpa rasa bersalah.",
                cost = 100,
                category = "Hadiah"
            ),
            ShopItem(
                title = "Beli Boba / Kopi Favorit",
                description = "Nikmati minuman favorit sebagai reward produktivitas.",
                cost = 200,
                category = "Hadiah"
            ),
            ShopItem(
                title = "Health Potion (Sembuhkan 40 HP)",
                description = "Pulihkan HP setelah terkena penalti di Hell Mode.",
                cost = 80,
                category = "Potion",
                hpRestoreAmount = 40
            ),
            ShopItem(
                title = "Rest Day Pass (Bebas Quest 1 Hari)",
                description = "Ambil hari libur penuh dari semua tugas.",
                cost = 500,
                category = "Hadiah"
            ),
            ShopItem(
                title = "Gelar Eksklusif: Lord of Productivity",
                description = "Buka gelar kustom untuk profil MMORPG kamu.",
                cost = 1000,
                category = "Title",
                titleReward = "Lord of Productivity"
            )
        )
    }
}
