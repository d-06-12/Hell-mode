package com.example.hell_mode.model

import java.util.UUID

data class FinancialBook(
    val id: String = UUID.randomUUID().toString(),
    var name: String,
    var description: String = "",
    var icon: String = "book",
    var colorHex: String = "#FFC107"
)
