package com.example.hell_mode.model

import java.util.UUID

enum class TransactionType {
    INCOME,   // Pemasukan
    EXPENSE   // Pengeluaran
}

data class RealMoneyTransaction(
    val id: String = UUID.randomUUID().toString(),
    var title: String,
    var amount: Long, // Positive magnitude in Rupiah
    var type: TransactionType = TransactionType.EXPENSE,
    var category: String = "Lainnya",
    var bookId: String = "default_book",
    var bookName: String = "Buku Utama / Kas",
    var note: String = "",
    var timestamp: Long = System.currentTimeMillis()
)
