package com.example.hell_mode.model

import java.util.UUID

data class ShopItem(
    val id: String = UUID.randomUUID().toString(),
    var title: String,
    var description: String,
    var cost: Int, // Gold/Saldo requirement
    var icon: String = "gift",
    var category: String = "Hadiah", // Hadiah, Potion, Title
    var hpRestoreAmount: Int = 0,
    var titleReward: String? = null
)
